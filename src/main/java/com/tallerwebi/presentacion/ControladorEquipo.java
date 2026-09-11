package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorEquipo extends ControladorBase {

  @Autowired
  public ControladorEquipo(ServicioRelacionAmistad servicioRelacionAmistad) {
    super(servicioRelacionAmistad);
  }

  @RequestMapping(path = "/agregar-equipo", method = RequestMethod.GET)
  public ModelAndView agregarEquipo(HttpServletRequest request) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Map<String, Object> modelo = modeloBase(usuario);
    modelo.put("amigos", servicioRelacionAmistad.listarAmigosDe(usuario));
    return new ModelAndView("agregarEquipo", modelo);
  }

  @RequestMapping(path = "/agregar-equipo", method = RequestMethod.POST)
  public ModelAndView crearEquipo(HttpServletRequest request) {
    if (usuarioDeSesion(request) == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    // TODO: persistir el equipo cuando exista el servicio correspondiente
    String aviso = URLEncoder.encode("Equipo creado correctamente", StandardCharsets.UTF_8);
    return new ModelAndView("redirect:/dashboard?aviso=" + aviso);
  }

  @RequestMapping(path = "/ranking-equipos", method = RequestMethod.GET)
  public ModelAndView rankingEquipos(HttpServletRequest request) {
    return vistaProtegida(request, "rankingEquipos");
  }
}
