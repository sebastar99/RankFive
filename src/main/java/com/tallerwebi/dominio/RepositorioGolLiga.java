package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioGolLiga {
  void guardar(GolLiga gol);

  List<GolLiga> listarPorLiga(Long ligaId);

  void borrarPorPartido(Long partidoId);

  long contarGolesDe(Long usuarioId);

  long contarAsistenciasDe(Long usuarioId);
}
