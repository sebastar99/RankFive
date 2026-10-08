package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ImagenGenerada;
import com.tallerwebi.dominio.ServicioIA;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

@RestController
@RequestMapping("/api/ia")
public class ControladorIA {

  private static final MediaType TEXTO_UTF_8 = new MediaType(
    MediaType.TEXT_PLAIN,
    StandardCharsets.UTF_8
  );

  private static final Logger LOGGER = LoggerFactory.getLogger(ControladorIA.class);

  private final ServicioIA servicioIA;
  private final ServicioPartido servicioPartido;

  @Autowired
  public ControladorIA(ServicioIA servicioIA, ServicioPartido servicioPartido) {
    this.servicioIA = servicioIA;
    this.servicioPartido = servicioPartido;
  }

  @PostMapping("/partido/{id}/previa")
  public ResponseEntity<String> generarPrevia(
    @PathVariable("id") Long id,
    HttpServletRequest request
  ) {
    if (!esParticipante(request, id)) {
      return respuestaDeTexto(403, "No tenés acceso a la previa de este partido.");
    }
    try {
      String previa = servicioIA.generarPreviaPartido(id);

      return ResponseEntity.ok().contentType(TEXTO_UTF_8).body(previa);
    } catch (Exception e) {
      return respuestaDeTexto(500, "Error generando previa: " + e.getMessage());
    }
  }

  @PostMapping("/partido/{id}/banner")
  public ResponseEntity<?> generarBanner(@PathVariable("id") Long id, HttpServletRequest request) {
    if (!esParticipante(request, id)) {
      return ResponseEntity.status(403).build();
    }
    try {
      ImagenGenerada banner = servicioIA.generarBannerPartido(id);
      return ResponseEntity
        .ok()
        .contentType(MediaType.parseMediaType(banner.tipoContenido()))
        .body(banner.datos());
    } catch (Exception e) {
      LOGGER.error("No se pudo generar el banner del partido {}", id, e);
      return respuestaDeTexto(500, mensajeDeError(e));
    }
  }

  private String mensajeDeError(Exception exception) {
    if (exception instanceof RestClientResponseException errorRemoto) {
      return errorRemoto.getResponseBodyAsString(StandardCharsets.UTF_8);
    }
    Throwable causa = exception.getCause();
    if (causa instanceof RestClientResponseException errorRemoto) {
      return errorRemoto.getResponseBodyAsString(StandardCharsets.UTF_8);
    }
    return exception.getMessage();
  }

  private ResponseEntity<String> respuestaDeTexto(int estado, String mensaje) {
    return ResponseEntity.status(estado).contentType(TEXTO_UTF_8).body(mensaje);
  }

  private boolean esParticipante(HttpServletRequest request, Long partidoId) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return false;
    }
    Usuario usuario = (Usuario) session.getAttribute("usuario");
    return (
      usuario != null &&
      servicioPartido
        .listarPartidosDe(usuario)
        .stream()
        .anyMatch(partido -> partidoId.equals(partido.getId()))
    );
  }
}
