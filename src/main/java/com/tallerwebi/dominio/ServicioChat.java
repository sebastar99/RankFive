package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioChat {
  Mensaje enviarMensaje(Long emisorId, Long destinatarioId, String contenido);
  String obtenerNombreUsuario(Long usuarioId);
  List<Mensaje> obtenerConversacion(Long usuarioId, Long otroUsuarioId);
}
