package com.tallerwebi.dominio;

import java.util.List;

public record DetallesEncuentro(
  List<Long> golesLocal,
  List<Long> asistenciasLocal,
  List<String> tiposTarjetasLocal,
  List<Long> tarjetasLocal,
  List<Long> golesVisitante,
  List<Long> asistenciasVisitante,
  List<String> tiposTarjetasVisitante,
  List<Long> tarjetasVisitante
) {
  public boolean estaVacio() {
    return (
      vacia(golesLocal) &&
      vacia(asistenciasLocal) &&
      vacia(tarjetasLocal) &&
      vacia(golesVisitante) &&
      vacia(asistenciasVisitante) &&
      vacia(tarjetasVisitante)
    );
  }

  private static boolean vacia(List<Long> ids) {
    return ids == null || ids.stream().allMatch(java.util.Objects::isNull);
  }
}
