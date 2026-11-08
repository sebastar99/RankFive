package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioGolTorneo {
  void guardar(GolTorneo gol);

  List<GolTorneo> listarPorTorneo(Long torneoId);

  void borrarPorEncuentro(Long encuentroId);

  long contarGolesDe(Long usuarioId);

  long contarAsistenciasDe(Long usuarioId);
}
