package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioEstadisticasPartido;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorEstadisticasPartido {

  private final ServicioEstadisticasPartido servicio;

  @Autowired
  public ControladorEstadisticasPartido(ServicioEstadisticasPartido servicio) {
    this.servicio = servicio;
  }

  @GetMapping("/partidos/{id}/estadisticas")
  public ModelAndView verEstadisticas(HttpServletRequest request, @PathVariable("id") Long id) {
    HttpSession sesion = request.getSession(false);

    Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    return servicio
      .obtener(id, usuario)
      .map(estadisticas -> new ModelAndView("estadisticasPartido", "estadisticas", estadisticas))
      .orElseGet(() ->
        new ModelAndView("redirect:/partidos?aviso=No%20ten%C3%A9s%20acceso%20a%20ese%20partido")
      );
  }
}
