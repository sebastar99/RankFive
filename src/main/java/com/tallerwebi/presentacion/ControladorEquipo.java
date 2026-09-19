package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.AlmacenamientoImagenes;
import com.tallerwebi.dominio.ResultadoCreacionEquipo;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.presentacion.DTO.DatosEquipo;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorEquipo extends ControladorBase {

  private static final String VISTA_AGREGAR = "agregarEquipo";

  private final ServicioEquipo servicioEquipo;
  private final AlmacenamientoImagenes almacenamientoImagenes;

  @Autowired
  public ControladorEquipo(
    ServicioRelacionAmistad servicioRelacionAmistad,
    ServicioEquipo servicioEquipo,
    AlmacenamientoImagenes almacenamientoImagenes
  ) {
    super(servicioRelacionAmistad);
    this.servicioEquipo = servicioEquipo;
    this.almacenamientoImagenes = almacenamientoImagenes;
  }

  @RequestMapping(path = "/agregar-equipo", method = RequestMethod.GET)
  public ModelAndView agregarEquipo(HttpServletRequest request) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Map<String, Object> modelo = modeloBase(usuario);
    modelo.put("amigos", servicioRelacionAmistad.listarAmigosDe(usuario));
    modelo.put("datosEquipo", new DatosEquipo());
    return new ModelAndView(VISTA_AGREGAR, modelo);
  }

  @RequestMapping(path = "/agregar-equipo", method = RequestMethod.POST)
  public ModelAndView crearEquipo(
    HttpServletRequest request,
    @ModelAttribute("datosEquipo") DatosEquipo datosEquipo,
    @RequestParam(name = "escudo", required = false) MultipartFile escudo
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    String fotoUrl = guardarEscudo(escudo);
    ResultadoCreacionEquipo resultado = servicioEquipo.crearEquipo(
      usuario,
      datosEquipo.getNombre(),
      datosEquipo.getCantidad(),
      fotoUrl,
      datosEquipo.getJugadores()
    );

    if (!resultado.fueExitoso()) {
      Map<String, Object> modelo = modeloBase(usuario);
      modelo.put("amigos", servicioRelacionAmistad.listarAmigosDe(usuario));
      modelo.put("datosEquipo", datosEquipo);
      modelo.put("error", resultado.getMensaje());
      return new ModelAndView(VISTA_AGREGAR, modelo);
    }
    return redirigirAlDashboard(resultado.getMensaje());
  }

  @RequestMapping(path = "/equipos/invitaciones/aceptar", method = RequestMethod.POST)
  public ModelAndView aceptarInvitacion(
    HttpServletRequest request,
    @RequestParam(name = "invitacionId") Long invitacionId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean aceptada = servicioEquipo.aceptarInvitacion(invitacionId, usuario);
    return redirigirAlDashboard(
      aceptada ? "Te sumaste al equipo" : "No se pudo aceptar la invitación"
    );
  }

  @RequestMapping(path = "/equipos/invitaciones/rechazar", method = RequestMethod.POST)
  public ModelAndView rechazarInvitacion(
    HttpServletRequest request,
    @RequestParam(name = "invitacionId") Long invitacionId
  ) {
    Usuario usuario = usuarioDeSesion(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    boolean rechazada = servicioEquipo.rechazarInvitacion(invitacionId, usuario);
    return redirigirAlDashboard(
      rechazada ? "Invitación rechazada" : "No se pudo rechazar la invitación"
    );
  }

  private String guardarEscudo(MultipartFile escudo) {
    if (escudo == null || escudo.isEmpty()) {
      return null;
    }
    try {
      return almacenamientoImagenes.guardarEscudo(escudo.getOriginalFilename(), escudo.getBytes());
    } catch (IOException e) {
      return null;
    }
  }

  private ModelAndView redirigirAlDashboard(String aviso) {
    String avisoCodificado = URLEncoder.encode(aviso, StandardCharsets.UTF_8);
    return new ModelAndView("redirect:/dashboard?aviso=" + avisoCodificado);
  }

  @RequestMapping(path = "/ranking-equipos", method = RequestMethod.GET)
  public ModelAndView rankingEquipos(HttpServletRequest request) {
    return vistaProtegida(request, "rankingEquipos");
  }
}
