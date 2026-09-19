package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.InvitacionEquipo;
import com.tallerwebi.dominio.NotificacionPl;
import com.tallerwebi.dominio.ServicioEquipo;
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
  private final ServicioEquipo servicioEquipo;

  @Autowired
  public NotificacionesAdvice(ServicioPartido servicioPartido, ServicioEquipo servicioEquipo) {
    this.servicioPartido = servicioPartido;
    this.servicioEquipo = servicioEquipo;
  }

  @ModelAttribute("avisosPl")
  public List<NotificacionPl> avisosPl(HttpServletRequest request) {
    Usuario usuario = usuarioDe(request);
    if (usuario == null) {
      return Collections.emptyList();
    }
    return servicioPartido.listarNotificacionesNoLeidas(usuario);
  }

  @ModelAttribute("invitacionesEquipo")
  public List<InvitacionEquipo> invitacionesEquipo(HttpServletRequest request) {
    Usuario usuario = usuarioDe(request);
    if (usuario == null) {
      return Collections.emptyList();
    }
    return servicioEquipo.listarInvitacionesPendientes(usuario);
  }

  private static Usuario usuarioDe(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }
    return (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
  }
}
