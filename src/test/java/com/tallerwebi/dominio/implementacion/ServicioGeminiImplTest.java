package com.tallerwebi.dominio.implementacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.ImagenGenerada;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
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
}
