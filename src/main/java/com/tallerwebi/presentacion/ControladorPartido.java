package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPartido {

  private static final String ATRIBUTO_USUARIO = "usuario";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String ERROR_CREACION =
    "No se pudo crear el partido. Revisá los equipos elegidos.";
  private static final String EQUIPO_A = "A";
  private static final String VISTA_PARTIDOS = "partidos";
  private static final String AVISO = "aviso";

  private final ServicioPartido servicioPartido;
  private final ServicioRelacionAmistad servicioRelacionAmistad;

  @Autowired
  public ControladorPartido(
    ServicioPartido servicioPartido,
    ServicioRelacionAmistad servicioRelacionAmistad
  ) {
    this.servicioPartido = servicioPartido;
    this.servicioRelacionAmistad = servicioRelacionAmistad;
  }

  @RequestMapping(path = "/partidos", method = RequestMethod.GET)
  public ModelAndView verPartidos(
    HttpServletRequest request,
    @RequestParam(name = "aviso", required = false) String aviso
  ) {
    Usuario usuarioActual = usuarioDeSesion(request);
    if (usuarioActual == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Map<String, Object> modelo = modeloBase(usuarioActual);
    modelo.put(AVISO, aviso);
    return new ModelAndView(VISTA_PARTIDOS, modelo);
  }

  @RequestMapping(path = "/partidos/notificaciones/leidas", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void marcarNotificacionesLeidas(HttpServletRequest request) {
    Usuario usuarioActual = usuarioDeSesion(request);
    if (usuarioActual != null) {
      servicioPartido.marcarNotificacionesLeidas(usuarioActual);
    }
  }

  @RequestMapping(path = "/partidos/crear", method = RequestMethod.POST)
  public ModelAndView crearPartido(
    HttpServletRequest request,
    @RequestParam("miEquipo") String miEquipo,
    @RequestParam("formato") Integer formato,
    @RequestParam(name = "equipoAIds", required = false) List<Long> equipoAIds,
    @RequestParam(name = "equipoBIds", required = false) List<Long> equipoBIds
  ) {
    Usuario usuarioActual = usuarioDeSesion(request);
    if (usuarioActual == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    List<Usuario> amigos = servicioRelacionAmistad.listarAmigosDe(usuarioActual);
    List<Usuario> equipoA = new ArrayList<>(filtrarPorIds(amigos, equipoAIds));
    List<Usuario> equipoB = new ArrayList<>(filtrarPorIds(amigos, equipoBIds));
    if (EQUIPO_A.equalsIgnoreCase(miEquipo)) {
      equipoA.add(usuarioActual);
    } else {
      equipoB.add(usuarioActual);
    }

    // Validacion de formato minimo por equipo
    int requeridoEquipo = (formato != null && List.of(5, 7, 8, 9, 11).contains(formato))
      ? formato
      : 5;
    // tamaños reales ya incluyen al usuario (agregado arriba)
    int tamAConUsuario = equipoA.size();
    int tamBConUsuario = equipoB.size();
    if (tamAConUsuario > requeridoEquipo || tamBConUsuario > requeridoEquipo) {
      Map<String, Object> modelo = modeloBase(usuarioActual);
      modelo.put("error", "Un equipo supera el máximo de " + requeridoEquipo + " jugadores");
      return new ModelAndView(VISTA_PARTIDOS, modelo);
    }

    Partido partido = servicioPartido.crearPartido(equipoA, equipoB);
    if (partido == null) {
      Map<String, Object> modelo = modeloBase(usuarioActual);
      modelo.put("error", ERROR_CREACION);
      return new ModelAndView(VISTA_PARTIDOS, modelo);
    }
    return redirigirAPartidos("Partido creado correctamente");
  }

  @RequestMapping(path = "/partidos/registrar-resultado", method = RequestMethod.POST)
  public ModelAndView registrarResultado(
    HttpServletRequest request,
    @RequestParam("partidoId") Long partidoId,
    @RequestParam("golesEquipoA") int golesEquipoA,
    @RequestParam("golesEquipoB") int golesEquipoB
  ) {
    Usuario usuarioActual = usuarioDeSesion(request);
    if (usuarioActual == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    boolean registrado = servicioPartido.registrarResultado(partidoId, golesEquipoA, golesEquipoB);
    if (registrado) {
      actualizarUsuarioEnSesion(request, usuarioActual);
    }
    return redirigirAPartidos(
      registrado ? "Resultado registrado, PL actualizado" : "No se pudo registrar el resultado"
    );
  }

  private void actualizarUsuarioEnSesion(HttpServletRequest request, Usuario usuarioActual) {
    Usuario actualizado = servicioPartido.buscarPorEmail(usuarioActual.getEmail());
    if (actualizado == null) {
      return;
    }
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.setAttribute(ATRIBUTO_USUARIO, actualizado);
    }
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
    modelo.put("amigos", servicioRelacionAmistad.listarAmigosDe(usuarioActual));
    modelo.put("historial", servicioPartido.listarPartidosDe(usuarioActual));
    modelo.put(
      "solicitudesPendientes",
      servicioRelacionAmistad.listarSolicitudesPendientes(usuarioActual)
    );
    modelo.put(
      "cantidadSolicitudes",
      servicioRelacionAmistad.contarSolicitudesPendientes(usuarioActual)
    );
    return modelo;
  }

  private List<Usuario> filtrarPorIds(List<Usuario> amigos, List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return Collections.emptyList();
    }
    return amigos.stream().filter(amigo -> ids.contains(amigo.getId())).toList();
  }

  private ModelAndView redirigirAPartidos(String aviso) {
    String avisoCodificado = URLEncoder.encode(aviso, StandardCharsets.UTF_8);
    return new ModelAndView("redirect:/partidos?aviso=" + avisoCodificado);
  }
}
