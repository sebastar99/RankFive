package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioPartido {
  void guardar(Partido partido);

  void actualizar(Partido partido);

  Partido buscarPorId(Long id);

  List<Partido> listarPartidosDe(Long usuarioId);
}
