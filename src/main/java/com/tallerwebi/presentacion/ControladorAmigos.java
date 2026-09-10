package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Amistad;
import com.tallerwebi.dominio.ResultadoSolicitud;
import com.tallerwebi.dominio.ServicioAmigos;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
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
  private static final String REDIRECT_LOGIN = "redirect:/login";

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
    Usuario usuarioActual = usuarioDeSesion(request);
    if (usuarioActual == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Map<String, Object> modelo = modeloBase(usuarioActual);
    if (nombreUsuario != null && !nombreUsuario.isBlank()) {
      Usuario amigoEncontrado = servicioAmigos.buscarAmigoPorNombreDeUsuario(nombreUsuario);
      if (amigoEncontrado != null) {
        modelo.put("amigo", amigoEncontrado);
      } else {
        modelo.put("error", "Usuario no encontrado");
      }
    }
    return new ModelAndView("buscarAmigos", modelo);
  }

  @RequestMapping(path = "/enviar-solicitud", method = RequestMethod.POST)
  public ModelAndView enviarSolicitud(
    HttpServletRequest request,
    @RequestParam(name = "nombreUsuario") String nombreUsuario
  ) {
    Usuario usuarioActual = usuarioDeSesion(request);
    if (usuarioActual == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Map<String, Object> modelo = modeloBase(usuarioActual);
    Usuario destinatario = servicioAmigos.buscarAmigoPorNombreDeUsuario(nombreUsuario);
    if (destinatario == null) {
      modelo.put("error", "Usuario no encontrado");
      return new ModelAndView("buscarAmigos", modelo);
    }

    modelo.put("amigo", destinatario);
    ResultadoSolicitud resultado = servicioRelacionAmistad.enviarSolicitud(
      usuarioActual,
      destinatario
    );
    if (resultado.fueExitosa()) {
      modelo.put("ok", resultado.getMensaje());
    } else {
      modelo.put("error", resultado.getMensaje());
    }
    modelo.put("cantidadSolicitudes", contarPendientes(usuarioActual));
    return new ModelAndView("buscarAmigos", modelo);
  }

  @RequestMapping(path = "/aceptar-solicitud", method = RequestMethod.POST)
  public ModelAndView aceptarSolicitud(
    HttpServletRequest request,
    @RequestParam(name = "solicitudId") Long solicitudId
  ) {
    Usuario usuarioActual = usuarioDeSesion(request);
    if (usuarioActual == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean aceptada = servicioRelacionAmistad.aceptarSolicitud(solicitudId, usuarioActual);
    return redirigirAlDashboard(
      aceptada ? "Solicitud aceptada, ya son amigos" : "No se pudo aceptar la solicitud"
    );
  }

  @RequestMapping(path = "/rechazar-solicitud", method = RequestMethod.POST)
  public ModelAndView rechazarSolicitud(
    HttpServletRequest request,
    @RequestParam(name = "solicitudId") Long solicitudId
  ) {
    Usuario usuarioActual = usuarioDeSesion(request);
    if (usuarioActual == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean rechazada = servicioRelacionAmistad.rechazarSolicitud(solicitudId, usuarioActual);
    return redirigirAlDashboard(
      rechazada ? "Solicitud rechazada" : "No se pudo rechazar la solicitud"
    );
  }

  private Usuario usuarioDeSesion(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }
    return (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
  }

  private Map<String, Object> modeloBase(Usuario usuarioActual) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put(ATRIBUTO_USUARIO, usuarioActual);
    modelo.put("datosAmigos", new DatosAmigos());
    modelo.put("solicitudesPendientes", listarPendientes(usuarioActual));
    modelo.put("cantidadSolicitudes", contarPendientes(usuarioActual));
    return modelo;
  }

  private List<Amistad> listarPendientes(Usuario usuarioActual) {
    if (servicioRelacionAmistad == null) {
      return Collections.emptyList();
    }
    return servicioRelacionAmistad.listarSolicitudesPendientes(usuarioActual);
  }

  private long contarPendientes(Usuario usuarioActual) {
    if (servicioRelacionAmistad == null) {
      return 0L;
    }
    return servicioRelacionAmistad.contarSolicitudesPendientes(usuarioActual);
  }

  private ModelAndView redirigirAlDashboard(String aviso) {
    String avisoCodificado = URLEncoder.encode(aviso, StandardCharsets.UTF_8);
    return new ModelAndView("redirect:/dashboard?aviso=" + avisoCodificado);
  }
}
