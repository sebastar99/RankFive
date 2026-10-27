package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Mensaje;
import com.tallerwebi.dominio.RepositorioMensaje;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioChat;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioChatImpl implements ServicioChat {

  private static final int MAXIMO_CARACTERES = 2000;

  private final RepositorioMensaje repositorioMensaje;
  private final RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioChatImpl(
    RepositorioMensaje repositorioMensaje,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioMensaje = repositorioMensaje;
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Mensaje enviarMensaje(Long emisorId, Long destinatarioId, String contenido) {
    validarParticipantes(emisorId, destinatarioId);

    if (contenido == null || contenido.isBlank()) {
      throw new IllegalArgumentException("El mensaje no puede estar vacío.");
    }

    String contenidoLimpio = contenido.strip();

    if (contenidoLimpio.length() > MAXIMO_CARACTERES) {
      throw new IllegalArgumentException("El mensaje no puede superar los 2000 caracteres.");
    }

    Usuario emisor = obtenerUsuarioExistente(emisorId);
    Usuario destinatario = obtenerUsuarioExistente(destinatarioId);

    Mensaje mensaje = new Mensaje(emisor, destinatario, contenidoLimpio);

    repositorioMensaje.guardar(mensaje);

    return mensaje;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Mensaje> obtenerConversacion(Long usuarioId, Long otroUsuarioId) {
    validarParticipantes(usuarioId, otroUsuarioId);

    obtenerUsuarioExistente(usuarioId);
    obtenerUsuarioExistente(otroUsuarioId);

    return repositorioMensaje.buscarConversacion(usuarioId, otroUsuarioId);
  }

  private void validarParticipantes(Long usuarioId, Long otroUsuarioId) {
    if (usuarioId == null || otroUsuarioId == null) {
      throw new IllegalArgumentException("Es necesario indicar ambos usuarios.");
    }

    if (usuarioId.equals(otroUsuarioId)) {
      throw new IllegalArgumentException("La conversación debe ser con otro usuario.");
    }
  }

  private Usuario obtenerUsuarioExistente(Long usuarioId) {
    Usuario usuario = repositorioUsuario.buscarPorId(usuarioId);

    if (usuario == null) {
      throw new IllegalArgumentException("El usuario indicado no existe.");
    }

    return usuario;
  }

  @Override
  @Transactional(readOnly = true)
  public String obtenerNombreUsuario(Long usuarioId) {
    if (usuarioId == null) {
      throw new IllegalArgumentException("Es necesario indicar un usuario.");
    }

    Usuario usuario = obtenerUsuarioExistente(usuarioId);

    if (usuario.getPerfil() != null) {
      String nombre = usuario.getPerfil().getNombreUsuario();

      if (nombre != null && !nombre.isBlank()) {
        return nombre;
      }
    }

    return "Usuario #" + usuario.getId();
  }
}
