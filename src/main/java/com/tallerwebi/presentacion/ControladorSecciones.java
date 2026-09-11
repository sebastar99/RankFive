package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioRelacionAmistad;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorSecciones extends ControladorBase {

  @Autowired
  public ControladorSecciones(ServicioRelacionAmistad servicioRelacionAmistad) {
    super(servicioRelacionAmistad);
  }

  @RequestMapping(path = "/torneos", method = RequestMethod.GET)
  public ModelAndView torneos(HttpServletRequest request) {
    return vistaProtegida(request, "ligaTorneos");
  }

  @RequestMapping(path = "/historial", method = RequestMethod.GET)
  public ModelAndView historial(HttpServletRequest request) {
    return vistaProtegida(request, "historial");
  }
}
