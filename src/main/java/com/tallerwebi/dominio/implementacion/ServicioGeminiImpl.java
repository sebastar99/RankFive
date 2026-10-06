package com.tallerwebi.dominio.implementacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.ImagenGenerada;
import com.tallerwebi.dominio.ServicioGemini;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ServicioGeminiImpl implements ServicioGemini {

  private static final String URL =
    "https://generativelanguage.googleapis.com" + "/v1beta/interactions";

  private static final String MODELO = "gemini-3.6-flash";
  private static final String MODELO_IMAGEN = "gemini-3.1-flash-image";

  private static final int MAX_INTENTOS = 3;

  private static final long ESPERA_ENTRE_INTENTOS = 2000;

  private static final String TIPO_TEXTO = "text";

  private static final String TIPO_IMAGEN = "image";

  private static final String CAMPO_TIPO = "type";

  private static final String TIPO_MODELO_OUTPUT = "model_output";

  private final RestTemplate restTemplate;

  @Value("${GEMINI_API_KEY:}")
  private String apiKey;

  @Autowired
  public ServicioGeminiImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public String generarTexto(String prompt) throws JsonProcessingException {
    validarApiKey();

    return extraerRespuesta(ejecutarConReintentos(crearRequest(prompt, MODELO, false)));
  }

  @Override
  public ImagenGenerada generarImagen(String prompt) throws JsonProcessingException {
    validarApiKey();
    return extraerImagen(ejecutarConReintentos(crearRequest(prompt, MODELO_IMAGEN, true)));
  }

  private void validarApiKey() {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException("No se configuró la variable GEMINI_API_KEY");
    }
  }

  private HttpEntity<String> crearRequest(String prompt, String modelo, boolean requiereImagen)
    throws JsonProcessingException {
    @SuppressWarnings("PMD.LooseCoupling")
    HttpHeaders headers = crearHeaders();

    Map<String, Object> body = new HashMap<>();

    body.put("model", modelo);
    body.put("input", prompt);
    if (requiereImagen) {
      body.put("response_format", formatoBanner());
    }

    ObjectMapper mapper = new ObjectMapper();

    String requestBody = mapper.writeValueAsString(body);

    return new HttpEntity<>(requestBody, headers);
  }

  private Map<String, String> formatoBanner() {
    Map<String, String> formato = new HashMap<>();
    formato.put(CAMPO_TIPO, TIPO_IMAGEN);
    formato.put("aspect_ratio", "16:9");
    return formato;
  }

  @SuppressWarnings("PMD.LooseCoupling")
  private HttpHeaders crearHeaders() {
    HttpHeaders headers = new HttpHeaders();

    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("x-goog-api-key", apiKey);

    return headers;
  }

  private String ejecutarConReintentos(HttpEntity<String> request) {
    Exception ultimoError = null;

    for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
      try {
        return ejecutarRequest(request);
      } catch (HttpServerErrorException e) {
        ultimoError = e;

        if (!esError503(e) || esUltimoIntento(intento)) {
          throw e;
        }

        esperarAntesDeReintentar(intento);
      }
    }

    throw new IllegalStateException(
      "No se pudo obtener respuesta de Gemini después de " + MAX_INTENTOS + " intentos.",
      ultimoError
    );
  }

  private String ejecutarRequest(HttpEntity<String> request) {
    return restTemplate.postForObject(URL, request, String.class);
  }

  private boolean esError503(HttpServerErrorException exception) {
    return exception.getStatusCode().value() == 503;
  }

  private boolean esUltimoIntento(int intento) {
    return intento == MAX_INTENTOS;
  }

  private void esperarAntesDeReintentar(int intento) {
    long espera = ESPERA_ENTRE_INTENTOS * intento;

    try {
      Thread.sleep(espera);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      throw new IllegalStateException("El intento de conexión con Gemini fue interrumpido.", e);
    }
  }

  private String extraerRespuesta(String json) {
    try {
      ObjectMapper mapper = new ObjectMapper();

      JsonNode root = mapper.readTree(json);

      JsonNode steps = root.path("steps");

      if (!steps.isArray()) {
        return "Gemini no devolvió una respuesta válida.";
      }

      return buscarTextoEnSteps(steps);
    } catch (Exception e) {
      return "Error procesando respuesta de Gemini: " + e.getMessage();
    }
  }

  private ImagenGenerada extraerImagen(String json) {
    try {
      JsonNode root = new ObjectMapper().readTree(json);
      ImagenGenerada imagenDirecta = imagenDe(root.path("output_image"));
      if (imagenDirecta != null) {
        return imagenDirecta;
      }
      return buscarImagenEnSteps(root.path("steps"));
    } catch (Exception e) {
      throw new IllegalStateException("Error procesando el banner generado.", e);
    }
  }

  private ImagenGenerada buscarImagenEnSteps(JsonNode steps) {
    for (JsonNode step : steps) {
      ImagenGenerada imagen = buscarImagenEnContenido(step);
      if (imagen != null) {
        return imagen;
      }
    }
    throw new IllegalStateException("Gemini no devolvió una imagen.");
  }

  private ImagenGenerada buscarImagenEnContenido(JsonNode step) {
    if (!esModeloOutput(step)) {
      return null;
    }
    for (JsonNode content : step.path("content")) {
      if (TIPO_IMAGEN.equals(content.path(CAMPO_TIPO).asText())) {
        ImagenGenerada imagen = imagenDe(content);
        if (imagen != null) {
          return imagen;
        }
      }
    }
    return null;
  }

  private ImagenGenerada imagenDe(JsonNode contenido) {
    String datos = contenido.path("data").asText();
    if (datos.isBlank()) {
      return null;
    }
    String tipo = contenido.path("mime_type").asText("image/png");
    return new ImagenGenerada(Base64.getDecoder().decode(datos), tipo);
  }

  private String buscarTextoEnSteps(JsonNode steps) {
    for (JsonNode step : steps) {
      if (!esModeloOutput(step)) {
        continue;
      }

      JsonNode content = step.path("content");

      if (!content.isArray()) {
        continue;
      }

      String texto = buscarTextoEnContent(content);

      if (!texto.isBlank()) {
        return texto;
      }
    }

    return "Gemini no devolvió texto.";
  }

  private boolean esModeloOutput(JsonNode step) {
    return TIPO_MODELO_OUTPUT.equals(step.path(CAMPO_TIPO).asText());
  }

  private String buscarTextoEnContent(JsonNode content) {
    for (JsonNode contentBlock : content) {
      if (esTexto(contentBlock)) {
        String texto = contentBlock.path("text").asText();

        if (!texto.isBlank()) {
          return texto;
        }
      }
    }

    return "";
  }

  private boolean esTexto(JsonNode contentBlock) {
    return TIPO_TEXTO.equals(contentBlock.path(CAMPO_TIPO).asText());
  }
}
