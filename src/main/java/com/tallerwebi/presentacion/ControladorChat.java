package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioChat;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.presentacion.DTO.DatosMensaje;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorChat {

  private final ServicioChat servicioChat;

  @Autowired
  public ControladorChat(ServicioChat servicioChat) {
    this.servicioChat = servicioChat;
  }

  @GetMapping("/chat/{otroUsuarioId}")
  public ModelAndView mostrarChat(
    @PathVariable("otroUsuarioId") Long otroUsuarioId,
    HttpServletRequest request
  ) {
    Usuario usuarioActual = obtenerUsuarioDeSesion(request);

    if (usuarioActual == null) {
      return new ModelAndView("redirect:/login");
    }

    List<DatosMensaje> mensajes = obtenerHistorial(usuarioActual.getId(), otroUsuarioId);

    Map<String, Object> modelo = new ModelMap();
    modelo.put("usuarioActualId", usuarioActual.getId());
    modelo.put("otroUsuarioId", otroUsuarioId);
    modelo.put("mensajes", mensajes);
    modelo.put("otroUsuarioNombre", servicioChat.obtenerNombreUsuario(otroUsuarioId));

    return new ModelAndView("chat", modelo);
  }

  @GetMapping("/chat/{otroUsuarioId}/mensajes")
  @ResponseBody
  public List<DatosMensaje> obtenerMensajes(
    @PathVariable("otroUsuarioId") Long otroUsuarioId,
    HttpServletRequest request
  ) {
    Usuario usuarioActual = obtenerUsuarioDeSesion(request);

    if (usuarioActual == null) {
      throw new ResponseStatusException(
        HttpStatus.UNAUTHORIZED,
        "Tenés que iniciar sesión para consultar los mensajes."
      );
    }

    return obtenerHistorial(usuarioActual.getId(), otroUsuarioId);
  }

  private List<DatosMensaje> obtenerHistorial(Long usuarioActualId, Long otroUsuarioId) {
    try {
      return servicioChat
        .obtenerConversacion(usuarioActualId, otroUsuarioId)
        .stream()
        .map(DatosMensaje::new)
        .toList();
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  private Usuario obtenerUsuarioDeSesion(HttpServletRequest request) {
    HttpSession sesion = request.getSession(false);

    if (sesion == null) {
      return null;
    }

    return (Usuario) sesion.getAttribute("usuario");
  }
}
