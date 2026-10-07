package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioGolPartido {
  void guardar(GolPartido gol);
  void borrarPorPartido(Long partidoId);
  List<GolPartido> listarPorPartido(Long partidoId);
}
