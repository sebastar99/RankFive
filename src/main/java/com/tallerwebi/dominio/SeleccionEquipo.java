package com.tallerwebi.dominio;

import java.util.List;

/** Resuelve la elección entre los equipos activos del usuario para un formato de competencia. */
public record SeleccionEquipo(Equipo equipo, ResultadoInscripcion error) {
  public static SeleccionEquipo elegir(List<Equipo> activos, Integer formato, Long equipoId) {
    if (activos == null || activos.isEmpty()) {
      return new SeleccionEquipo(null, ResultadoInscripcion.SIN_EQUIPO_ACTIVO);
    }
    List<Equipo> compatibles = compatibles(activos, formato);
    if (compatibles.isEmpty()) {
      return new SeleccionEquipo(null, ResultadoInscripcion.SIN_EQUIPO_COMPATIBLE);
    }
    if (equipoId == null) {
      return compatibles.size() == 1
        ? new SeleccionEquipo(compatibles.get(0), null)
        : new SeleccionEquipo(null, ResultadoInscripcion.SELECCIONAR_EQUIPO);
    }
    return compatibles
      .stream()
      .filter(equipo -> equipoId.equals(equipo.getId()))
      .findFirst()
      .map(equipo -> new SeleccionEquipo(equipo, null))
      .orElseGet(() -> new SeleccionEquipo(null, ResultadoInscripcion.EQUIPO_NO_DISPONIBLE));
  }

  public static List<Equipo> compatibles(List<Equipo> activos, Integer formato) {
    if (activos == null || formato == null) {
      return List.of();
    }
    return activos.stream().filter(equipo -> formato.equals(equipo.getFormato())).toList();
  }
}
