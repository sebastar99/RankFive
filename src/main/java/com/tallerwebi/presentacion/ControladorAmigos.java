package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioAmigos;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAmigos {

  private ServicioAmigos servicioAmigos;
  private ServicioRelacionAmistad servicioRelacionAmistad;
  private static final String ATRIBUTO_USUARIO = "usuario";

  @Autowired
  public ControladorAmigos(ServicioAmigos servicioAmigos) {
    this.servicioAmigos = servicioAmigos;
  }

  @Autowired(required = false)
  public void setServicioRelacionAmistad(ServicioRelacionAmistad servicioRelacionAmistad) {
    this.servicioRelacionAmistad = servicioRelacionAmistad;
  }

  @RequestMapping("/buscar")
  public ModelAndView buscarAmigos(
    HttpServletRequest request,
    @RequestParam(name = "nombreUsuario", required = false) String nombreUsuario
  ) {
    Object usuarioEnSesion = request.getSession(false) != null
      ? request.getSession(false).getAttribute(ATRIBUTO_USUARIO)
      : null;
    if (usuarioEnSesion == null) {
      return new ModelAndView("redirect:/login");
    }
    Map<String, Object> modelo = new ModelMap();
    if (nombreUsuario != null && !nombreUsuario.isBlank()) {
      Usuario amigoEncontrado = servicioAmigos.buscarAmigoPorNombreDeUsuario(nombreUsuario);
      if (amigoEncontrado != null) {
        modelo.put("amigo", amigoEncontrado);
      } else {
        modelo.put("error", "Usuario no encontrado");
      }
    }
    modelo.put("datosAmigos", new DatosAmigos());
    return new ModelAndView("buscarAmigos", modelo);
  }

  @RequestMapping(path = "/agregar-amigo", method = RequestMethod.POST)
  public ModelAndView agregarAmigo(
    HttpServletRequest request,
    @RequestParam(name = "nombreUsuario") String nombreUsuario
  ) {
    Object usuarioEnSesion = request.getSession(false) != null
      ? request.getSession(false).getAttribute(ATRIBUTO_USUARIO)
      : null;
    if (usuarioEnSesion == null) {
      return new ModelAndView("redirect:/login");
    }
    Map<String, Object> modelo = new ModelMap();
    Usuario amigo = servicioAmigos.buscarAmigoPorNombreDeUsuario(nombreUsuario);
    if (amigo == null) {
      modelo.put("error", "Usuario no encontrado");
    } else if (servicioRelacionAmistad == null) {
      modelo.put("error", "Servicio de amistad no disponible");
    } else {
      servicioRelacionAmistad.agregarAmigo((Usuario) usuarioEnSesion, amigo);
      modelo.put("amigo", amigo);
      modelo.put("ok", "Amigo agregado");
    }
    modelo.put("datosAmigos", new DatosAmigos());
    return new ModelAndView("buscarAmigos", modelo);
  }
}
