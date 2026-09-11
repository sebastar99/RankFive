package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class ControladorBase {

  protected static final String ATRIBUTO_USUARIO = "usuario";
  protected static final String REDIRECT_LOGIN = "redirect:/login";

  protected final ServicioRelacionAmistad servicioRelacionAmistad;

  protected ControladorBase(ServicioRelacionAmistad servicioRelacionAmistad) {
    this.servicioRelacionAmistad = servicioRelacionAmistad;
  }

  protected Usuario usuarioDeSesion(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }
    return (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
  }

  protected Map<String, Object> modeloBase(Usuario usuario) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put(ATRIBUTO_USUARIO, usuario);
    modelo.put(
      "solicitudesPendientes",
      servicioRelacionAmistad.listarSolicitudesPendientes(usuario)
    );
    modelo.put("cantidadSolicitudes", servicioRelacionAmistad.contarSolicitudesPendientes(usuario));
    return modelo;
  }

  protected ModelAndView vistaProtegida(HttpServletRequest request, String vista) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    return new ModelAndView(vista, modeloBase(usuario));
  }
}
