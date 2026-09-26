package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioPartidoLiga {
  void guardar(PartidoLiga partido);

  void actualizar(PartidoLiga partido);

  PartidoLiga buscarPorId(Long id);

  List<PartidoLiga> listarPorLiga(Long ligaId);
}
