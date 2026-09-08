package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

  @Autowired
  public ControladorLogin(ServicioLogin servicioLogin) {
    this.servicioLogin = servicioLogin;
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
  public ModelAndView irADashboard(HttpServletRequest request) {
    Usuario usuario = (Usuario) request.getSession().getAttribute(ATRIBUTO_USUARIO);
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }
    Integer pl = usuario.getPerfil() != null ? usuario.getPerfil().getPl() : null;
    int puntos = pl != null ? pl : 0;

    String rangoNombre;
    String rangoCss;
    String rangoIcon;
    if (puntos >= LEGEND_MIN) {
      rangoNombre = "Legendario";
      rangoCss = "rank-legend";
      rangoIcon = "bi-lightning-charge-fill";
    } else if (puntos >= DIAMOND_MIN) {
      rangoNombre = "Diamante";
      rangoCss = "rank-diamond";
      rangoIcon = "bi-gem";
    } else if (puntos >= PLATINUM_MIN) {
      rangoNombre = "Platino";
      rangoCss = "rank-plat";
      rangoIcon = "bi-diamond";
    } else if (puntos >= GOLD_MIN) {
      rangoNombre = "Oro";
      rangoCss = "rank-gold";
      rangoIcon = "bi-trophy-fill";
    } else if (puntos >= SILVER_MIN) {
      rangoNombre = "Plata";
      rangoCss = "rank-silver";
      rangoIcon = "bi-trophy";
    } else {
      rangoNombre = "Bronce";
      rangoCss = "rank-bronze";
      rangoIcon = "bi-award";
    }

    Map<String, Object> model = new ModelMap();
    model.put(ATRIBUTO_USUARIO, usuario);
    model.put("puntos", puntos);
    model.put("rangoNombre", rangoNombre);
    model.put("rangoCss", rangoCss);
    model.put("rangoIcon", rangoIcon);
    return new ModelAndView("dashboard", model);
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
