package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioMensaje {
  void guardar(Mensaje mensaje);

  List<Mensaje> buscarConversacion(Long usuarioId, Long otroUsuarioId);
}
