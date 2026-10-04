package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioGolLiga {
  void guardar(GolLiga gol);

  List<GolLiga> listarPorLiga(Long ligaId);

  long contarPor(Long partidoId, Long inscripcionId);
}
