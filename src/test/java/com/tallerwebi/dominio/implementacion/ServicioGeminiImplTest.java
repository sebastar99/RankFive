package com.tallerwebi.dominio.implementacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.ImagenGenerada;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class ServicioGeminiImplTest {

  private RestTemplate restTemplate;
  private ServicioGeminiImpl servicio;

  @BeforeEach
  void setUp() throws Exception {
    this.restTemplate = mock(RestTemplate.class);
    this.servicio = new ServicioGeminiImpl(restTemplate);
    // Inyectar apiKey simulada
    Field f = ServicioGeminiImpl.class.getDeclaredField("apiKey");
    f.setAccessible(true);
    f.set(servicio, "TEST_KEY");
  }

  @Test
  void generarTexto_extrae_contenido_de_steps() throws JsonProcessingException {
    String json =
      "{\n" +
      "  \"steps\": [\n" +
      "    {\n" +
      "      \"type\": \"model_output\",\n" +
      "      \"content\": [\n" +
      "        { \"type\": \"text\", \"text\": \"Hola mundo\" }\n" +
      "      ]\n" +
      "    }\n" +
      "  ]\n" +
      "}";

    when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn(json);

    String out = servicio.generarTexto("prompt");
    assertEquals("Hola mundo", out);
  }

  @Test
  void generarImagen_usa_output_image_directo() throws JsonProcessingException {
    // "YQ==" es Base64 de la letra 'a'
    String json =
      "{\n" +
      "  \"output_image\": {\n" +
      "    \"type\": \"image\",\n" +
      "    \"mime_type\": \"image/png\",\n" +
      "    \"data\": \"YQ==\"\n" +
      "  }\n" +
      "}";

    when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn(json);

    ImagenGenerada img = servicio.generarImagen("prompt");
    assertNotNull(img);
    assertEquals("image/png", img.tipoContenido());
    assertNotNull(img.datos());
    assertTrue(img.datos().length > 0);
  }

  @Test
  void rechazaLlamadasCuandoFaltaLaClave() throws Exception {
    configurarClave("");

    assertThrows(IllegalStateException.class, () -> servicio.generarTexto("prompt"));
    assertThrows(IllegalStateException.class, () -> servicio.generarImagen("prompt"));
    verifyNoInteractions(restTemplate);
  }

  @Test
  void informaCuandoGeminiNoDevuelveStepsValidos() throws Exception {
    when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn("{\"steps\":\"inválido\"}")
      .thenReturn("{\"steps\":[]}");

    assertEquals("Gemini no devolvió una respuesta válida.", servicio.generarTexto("prompt"));
    assertEquals("Gemini no devolvió texto.", servicio.generarTexto("prompt"));
  }

  @Test
  void omitePasosYBloquesSinTexto() throws Exception {
    String json =
      """
      {
        "steps": [
          {"type":"input","content":[{"type":"text","text":"ignorar"}]},
          {"type":"model_output","content":{"type":"text","text":"ignorar"}},
          {"type":"model_output","content":[{"type":"text","text":"  "}]}
        ]
      }
      """;
    when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn(json);

    assertEquals("Gemini no devolvió texto.", servicio.generarTexto("prompt"));
  }

  @Test
  void extraeImagenDeUnPasoYUsaElTipoPredeterminado() throws Exception {
    String json =
      """
      {
        "steps": [
          {"type":"input","content":[{"type":"image","data":"YQ=="}]},
          {"type":"model_output","content":[
            {"type":"text","text":"sin imagen"},
            {"type":"image","data":""},
            {"type":"image","data":"YQ=="}
          ]}
        ]
      }
      """;
    when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn(json);

    ImagenGenerada imagen = servicio.generarImagen("prompt");

    assertEquals("image/png", imagen.tipoContenido());
    assertArrayEquals(new byte[] { 'a' }, imagen.datos());
  }

  @Test
  void informaCuandoLaRespuestaNoContieneUnaImagen() throws Exception {
    when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn("{\"steps\":[]}");

    IllegalStateException error = assertThrows(
      IllegalStateException.class,
      () -> servicio.generarImagen("prompt")
    );

    assertEquals("Error procesando el banner generado.", error.getMessage());
    assertEquals("Gemini no devolvió una imagen.", error.getCause().getMessage());
  }

  @Test
  void interrumpirUnReintento503RestauraElEstadoDelHilo() throws Exception {
    HttpServerErrorException error = HttpServerErrorException.create(
      HttpStatus.SERVICE_UNAVAILABLE,
      "Unavailable",
      HttpHeaders.EMPTY,
      new byte[0],
      StandardCharsets.UTF_8
    );
    when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenThrow(error);
    Thread.interrupted();
    Thread.currentThread().interrupt();

    try {
      IllegalStateException excepcion = assertThrows(
        IllegalStateException.class,
        () -> servicio.generarTexto("prompt")
      );

      assertEquals("El intento de conexión con Gemini fue interrumpido.", excepcion.getMessage());
      assertTrue(Thread.currentThread().isInterrupted());
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  void noReintentaErroresDelServidorQueNoSean503() throws Exception {
    HttpServerErrorException error = HttpServerErrorException.create(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Internal Server Error",
      HttpHeaders.EMPTY,
      new byte[0],
      StandardCharsets.UTF_8
    );
    when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenThrow(error);

    assertSame(error, assertThrows(RuntimeException.class, () -> servicio.generarTexto("prompt")));
    verify(restTemplate, times(1))
      .postForObject(anyString(), any(HttpEntity.class), eq(String.class));
  }

  private void configurarClave(String clave) throws Exception {
    Field field = ServicioGeminiImpl.class.getDeclaredField("apiKey");
    field.setAccessible(true);
    field.set(servicio, clave);
  }
}
