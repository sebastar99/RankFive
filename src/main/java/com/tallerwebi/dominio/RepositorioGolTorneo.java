package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioGolTorneo {
  void guardar(GolTorneo gol);

  List<GolTorneo> listarPorTorneo(Long torneoId);

  long contarPor(Long encuentroId, Long inscripcionId);
}
