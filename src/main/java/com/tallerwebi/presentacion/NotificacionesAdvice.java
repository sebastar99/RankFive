package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.NotificacionPl;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NotificacionesAdvice {

  private static final String ATRIBUTO_USUARIO = "usuario";

  private final ServicioPartido servicioPartido;

  @Autowired
  public NotificacionesAdvice(ServicioPartido servicioPartido) {
    this.servicioPartido = servicioPartido;
  }

  @ModelAttribute("avisosPl")
  public List<NotificacionPl> avisosPl(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return Collections.emptyList();
    }
    Usuario usuario = (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null) {
      return Collections.emptyList();
    }
    return servicioPartido.listarNotificacionesNoLeidas(usuario);
  }
}
