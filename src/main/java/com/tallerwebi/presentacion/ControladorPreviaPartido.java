package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPreviaPartido {

  private static final String ATRIBUTO_USUARIO = "usuario";

  private final ServicioPartido servicioPartido;

  @Autowired
  public ControladorPreviaPartido(ServicioPartido servicioPartido) {
    this.servicioPartido = servicioPartido;
  }

  @RequestMapping(path = "/partidos/{id}/previa", method = RequestMethod.GET)
  public ModelAndView verPrevia(HttpServletRequest request, @PathVariable("id") Long partidoId) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }
    Partido partido = servicioPartido
      .listarPartidosDe(usuario)
      .stream()
      .filter(partidoDeUsuario -> partidoId.equals(partidoDeUsuario.getId()))
      .findFirst()
      .orElse(null);
    if (partido == null) {
      return new ModelAndView(
        "redirect:/partidos?aviso=No%20ten%C3%A9s%20acceso%20a%20ese%20partido"
      );
    }
    Map<String, Object> modelo = new ModelMap();
    modelo.put(ATRIBUTO_USUARIO, usuario);
    modelo.put("partido", partido);
    return new ModelAndView("PreviaPartido", modelo);
  }

  private Usuario usuarioDeSesion(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    return session == null ? null : (Usuario) session.getAttribute(ATRIBUTO_USUARIO);
  }
}
