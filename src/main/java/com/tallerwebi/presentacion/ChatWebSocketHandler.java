package com.tallerwebi.presentacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.Mensaje;
import com.tallerwebi.dominio.ServicioChat;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.presentacion.DTO.DatosMensaje;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@SuppressWarnings("PMD.CloseResource")
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

  private static final Logger LOGGER = Logger.getLogger(ChatWebSocketHandler.class.getName());

  private final ServicioChat servicioChat;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final Map<String, WebSocketSession> conexiones = new ConcurrentHashMap<>();

  @Autowired
  public ChatWebSocketHandler(ServicioChat servicioChat) {
    this.servicioChat = servicioChat;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    if (obtenerUsuarioAutenticado(session) == null) {
      cerrarConexion(session, CloseStatus.POLICY_VIOLATION);
      return;
    }

    session.setTextMessageSizeLimit(16384);

    WebSocketSession conexionSegura = new ConcurrentWebSocketSessionDecorator(
      session,
      10000,
      65536
    );

    conexiones.put(session.getId(), conexionSegura);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message)
    throws IOException {
    Long emisorId = obtenerUsuarioAutenticado(session);

    if (emisorId == null) {
      cerrarConexion(session, CloseStatus.POLICY_VIOLATION);
      return;
    }

    JsonNode datos = parsearJson(message);

    if (datos == null) {
      return;
    }

    Mensaje mensajeGuardado = procesarMensaje(emisorId, datos, session);

    if (mensajeGuardado == null) {
      return;
    }

    DatosMensaje respuesta = new DatosMensaje(mensajeGuardado);

    String json = objectMapper.writeValueAsString(Map.of("tipo", "mensaje", "datos", respuesta));

    entregarAParticipantes(respuesta.getEmisorId(), respuesta.getDestinatarioId(), json);
  }

  private JsonNode parsearJson(TextMessage message) {
    try {
      return objectMapper.readTree(message.getPayload());
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private Mensaje procesarMensaje(Long emisorId, JsonNode datos, WebSocketSession session) {
    if (!validarEstructuraJson(datos)) {
      enviarErrorSilencioso(session, "Se esperaba un objeto JSON.");
      return null;
    }

    JsonNode destinatario = datos.get("destinatarioId");
    JsonNode contenido = datos.get("contenido");

    if (!validarDatos(destinatario, contenido)) {
      enviarErrorSilencioso(session, "Indicá un destinatario válido y un contenido de texto.");
      return null;
    }

    return guardarMensaje(emisorId, destinatario, contenido, session);
  }

  private boolean validarEstructuraJson(JsonNode datos) {
    return datos != null && datos.isObject();
  }

  private Mensaje guardarMensaje(
    Long emisorId,
    JsonNode destinatario,
    JsonNode contenido,
    WebSocketSession session
  ) {
    try {
      return servicioChat.enviarMensaje(emisorId, destinatario.longValue(), contenido.textValue());
    } catch (IllegalArgumentException e) {
      enviarErrorSilencioso(session, e.getMessage());
      return null;
    } catch (RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Error al guardar un mensaje de chat", e);
      enviarErrorSilencioso(
        session,
        "No se pudo confirmar el guardado. Revisá el historial antes de reintentar."
      );
      return null;
    }
  }

  private void enviarErrorSilencioso(WebSocketSession session, String descripcion) {
    try {
      enviarError(session, descripcion);
    } catch (JsonProcessingException e) {
      LOGGER.log(Level.FINE, "Error al enviar error de chat", e);
    }
  }

  private boolean validarDatos(JsonNode destinatario, JsonNode contenido) {
    return (
      destinatario != null &&
      destinatario.isIntegralNumber() &&
      destinatario.canConvertToLong() &&
      destinatario.longValue() > 0 &&
      contenido != null &&
      contenido.isTextual()
    );
  }

  private void entregarAParticipantes(Long emisorId, Long destinatarioId, String json) {
    for (WebSocketSession conexion : conexiones.values()) {
      Long usuarioId = obtenerUsuarioAutenticado(conexion);

      if (usuarioId == null) {
        cerrarConexion(conexion, CloseStatus.POLICY_VIOLATION);
        continue;
      }

      if (usuarioId.equals(emisorId) || usuarioId.equals(destinatarioId)) {
        try {
          enviarTexto(conexion, json);
        } catch (Exception e) {
          LOGGER.log(Level.FINE, "Error al entregar mensaje", e);
        }
      }
    }
  }

  private void enviarError(WebSocketSession session, String descripcion)
    throws JsonProcessingException {
    String json = objectMapper.writeValueAsString(
      Map.of("tipo", "error", "descripcion", descripcion)
    );

    String sessionId = session.getId();
    WebSocketSession conexion = conexiones.get(sessionId);

    if (conexion != null) {
      enviarTexto(conexion, json);
    }
  }

  private void enviarTexto(WebSocketSession conexion, String json) {
    try {
      if (!conexion.isOpen()) {
        cerrarConexion(conexion, CloseStatus.NORMAL);
        return;
      }

      conexion.sendMessage(new TextMessage(json));
    } catch (IOException | RuntimeException e) {
      LOGGER.log(Level.FINE, "No se pudo entregar un mensaje de chat", e);
      cerrarConexion(conexion, CloseStatus.SERVER_ERROR);
    }
  }

  private Long obtenerUsuarioAutenticado(WebSocketSession conexion) {
    Object id = conexion.getAttributes().get(ChatHandshakeInterceptor.USUARIO_ID);

    Object referenciaSesion = conexion.getAttributes().get(ChatHandshakeInterceptor.SESION_HTTP);

    if (!(id instanceof Long) || !(referenciaSesion instanceof HttpSession)) {
      return null;
    }

    HttpSession sesionHttp = (HttpSession) referenciaSesion;

    try {
      Object usuarioEnSesion = sesionHttp.getAttribute("usuario");

      if (!(usuarioEnSesion instanceof Usuario)) {
        return null;
      }

      Usuario usuario = (Usuario) usuarioEnSesion;
      Long usuarioId = (Long) id;

      return usuarioId.equals(usuario.getId()) ? usuarioId : null;
    } catch (IllegalStateException e) {
      return null;
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    conexiones.remove(session.getId());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    cerrarConexion(session, CloseStatus.SERVER_ERROR);
  }

  private void cerrarConexion(WebSocketSession session, CloseStatus status) {
    conexiones.remove(session.getId());

    try {
      if (session.isOpen()) {
        session.close(status);
      }
    } catch (IOException e) {
      LOGGER.log(Level.FINE, "No se pudo cerrar la conexión de chat", e);
    }
  }
}
