package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

  public static final String USUARIO_ID = "usuarioId";
  public static final String SESION_HTTP = "sesionHttp";

  @Override
  public boolean beforeHandshake(
    ServerHttpRequest request,
    ServerHttpResponse response,
    WebSocketHandler wsHandler,
    Map<String, Object> attributes
  ) {
    if (!(request instanceof ServletServerHttpRequest)) {
      response.setStatusCode(HttpStatus.BAD_REQUEST);
      return false;
    }

    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

    HttpSession sesion = servletRequest.getServletRequest().getSession(false);

    if (sesion == null) {
      response.setStatusCode(HttpStatus.UNAUTHORIZED);
      return false;
    }

    try {
      Object usuarioEnSesion = sesion.getAttribute("usuario");

      if (!(usuarioEnSesion instanceof Usuario)) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
      }

      Usuario usuario = (Usuario) usuarioEnSesion;

      if (usuario.getId() == null) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
      }

      attributes.put(USUARIO_ID, usuario.getId());
      attributes.put(SESION_HTTP, sesion);

      return true;
    } catch (IllegalStateException e) {
      response.setStatusCode(HttpStatus.UNAUTHORIZED);
      return false;
    }
  }

  @Override
  public void afterHandshake(
    ServerHttpRequest request,
    ServerHttpResponse response,
    WebSocketHandler wsHandler,
    Exception exception
  ) {
    // No necesitamos realizar acciones después del handshake.
  }
}
