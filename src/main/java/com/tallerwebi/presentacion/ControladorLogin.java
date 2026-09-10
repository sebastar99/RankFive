package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Amistad;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLogin {

  private static final String ATRIBUTO_USUARIO = "usuario";
  private static final int LEGEND_MIN = 3501;
  private static final int DIAMOND_MIN = 3001;
  private static final int PLATINUM_MIN = 2501;
  private static final int GOLD_MIN = 2001;
  private static final int SILVER_MIN = 1501;
  private ServicioLogin servicioLogin;
  private ServicioRelacionAmistad servicioRelacionAmistad;

  @Autowired
  public ControladorLogin(ServicioLogin servicioLogin) {
    this.servicioLogin = servicioLogin;
  }

  @Autowired(required = false)
  public void setServicioRelacionAmistad(ServicioRelacionAmistad servicioRelacionAmistad) {
    this.servicioRelacionAmistad = servicioRelacionAmistad;
  }

  @RequestMapping("/login")
  public ModelAndView irALogin(HttpServletRequest request) {
    Object usuarioEnSesion = request.getSession(false) != null
      ? request.getSession(false).getAttribute(ATRIBUTO_USUARIO)
      : null;
    if (usuarioEnSesion != null) {
      return new ModelAndView("redirect:/dashboard");
    }
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosLogin", new DatosLogin());
    return new ModelAndView("login", modelo);
  }

  // Helper no-arg overload used by existing tests
  public ModelAndView irALogin() {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosLogin", new DatosLogin());
    return new ModelAndView("login", modelo);
  }

  @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
  public ModelAndView validarLogin(
    @ModelAttribute("datosLogin") DatosLogin datosLogin,
    HttpServletRequest request
  ) {
    Usuario usuarioBuscado = servicioLogin.consultarUsuario(
      datosLogin.getEmail(),
      datosLogin.getPassword()
    );
    if (usuarioBuscado != null) {
      request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
      request.getSession().setAttribute(ATRIBUTO_USUARIO, usuarioBuscado);
      return new ModelAndView("redirect:/dashboard");
    } else {
      Map<String, Object> model = new ModelMap();
      model.put("error", "Usuario o clave incorrecta");
      return new ModelAndView("login", model);
    }
  }

  @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
  public ModelAndView registrarme(@ModelAttribute(ATRIBUTO_USUARIO) Usuario usuario) {
    Map<String, Object> model = new ModelMap();
    try {
      servicioLogin.registrar(usuario);
    } catch (UsuarioExistente e) {
      model.put("error", "El usuario ya existe");
      model.put(ATRIBUTO_USUARIO, usuario);
      return new ModelAndView("nuevo-usuario", model);
    } catch (Exception e) {
      model.put("error", "Error al registrar el nuevo usuario");
      model.put(ATRIBUTO_USUARIO, usuario);
      return new ModelAndView("nuevo-usuario", model);
    }
    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
  public ModelAndView nuevoUsuario() {
    Map<String, Object> model = new ModelMap();
    model.put(ATRIBUTO_USUARIO, new Usuario());
    return new ModelAndView("nuevo-usuario", model);
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome() {
    return new ModelAndView("home");
  }

  @RequestMapping(path = "/dashboard", method = RequestMethod.GET)
  public ModelAndView irADashboard(
    HttpServletRequest request,
    @RequestParam(name = "aviso", required = false) String aviso
  ) {
    Usuario usuario = (Usuario) request.getSession().getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }
    int puntos = calcularPuntos(usuario);
    String[] rango = calcularRango(puntos);

    Map<String, Object> model = new ModelMap();
    model.put(ATRIBUTO_USUARIO, usuario);
    model.put("puntos", puntos);
    model.put("rangoNombre", rango[0]);
    model.put("rangoCss", rango[1]);
    model.put("rangoIcon", rango[2]);
    model.put("rankingAmigos", obtenerRankingAmigos(usuario));
    model.put("solicitudesPendientes", obtenerSolicitudesPendientes(usuario));
    model.put("cantidadSolicitudes", contarSolicitudesPendientes(usuario));
    model.put("aviso", aviso);
    return new ModelAndView("dashboard", model);
  }

  private int calcularPuntos(Usuario usuario) {
    if (usuario.getPerfil() == null || usuario.getPerfil().getPl() == null) {
      return 0;
    }
    return usuario.getPerfil().getPl();
  }

  private String[] calcularRango(int puntos) {
    if (puntos >= LEGEND_MIN) {
      return new String[] { "Legendario", "rank-legend", "bi-lightning-charge-fill" };
    }
    if (puntos >= DIAMOND_MIN) {
      return new String[] { "Diamante", "rank-diamond", "bi-gem" };
    }
    if (puntos >= PLATINUM_MIN) {
      return new String[] { "Platino", "rank-plat", "bi-diamond" };
    }
    if (puntos >= GOLD_MIN) {
      return new String[] { "Oro", "rank-gold", "bi-trophy-fill" };
    }
    if (puntos >= SILVER_MIN) {
      return new String[] { "Plata", "rank-silver", "bi-trophy" };
    }
    return new String[] { "Bronce", "rank-bronze", "bi-award" };
  }

  private List<Usuario> obtenerRankingAmigos(Usuario usuario) {
    if (servicioRelacionAmistad == null) {
      return Collections.emptyList();
    }
    return servicioRelacionAmistad.listarAmigosDe(usuario);
  }

  private List<Amistad> obtenerSolicitudesPendientes(Usuario usuario) {
    if (servicioRelacionAmistad == null) {
      return Collections.emptyList();
    }
    return servicioRelacionAmistad.listarSolicitudesPendientes(usuario);
  }

  private long contarSolicitudesPendientes(Usuario usuario) {
    if (servicioRelacionAmistad == null) {
      return 0L;
    }
    return servicioRelacionAmistad.contarSolicitudesPendientes(usuario);
  }

  @RequestMapping(path = "/logout", method = RequestMethod.GET)
  public ModelAndView logout(HttpServletRequest request) {
    if (request.getSession(false) != null) {
      request.getSession(false).invalidate();
    }
    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  public ModelAndView inicio() {
    return new ModelAndView("redirect:/home");
  }
}
