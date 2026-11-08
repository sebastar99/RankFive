package com.tallerwebi.dominio;

import com.tallerwebi.dominio.ConstructorDetalles.GolCargado;
import com.tallerwebi.dominio.ConstructorDetalles.TarjetaCargada;
import java.util.List;
import java.util.Optional;

public record ResolucionDetalles(
  List<GolCargado> golesLocal,
  List<TarjetaCargada> tarjetasLocal,
  List<GolCargado> golesVisitante,
  List<TarjetaCargada> tarjetasVisitante
) {
  public static Optional<ResolucionDetalles> de(
    DetallesEncuentro detalles,
    Equipo local,
    Equipo visitante,
    Integer marcadorLocal,
    Integer marcadorVisitante
  ) {
    if (detalles == null || local == null || visitante == null) {
      return Optional.empty();
    }
    var plantelLocal = ConstructorDetalles.plantelDe(local);
    var plantelVisitante = ConstructorDetalles.plantelDe(visitante);
    List<GolCargado> golesLocal = ConstructorDetalles.goles(
      plantelLocal,
      detalles.golesLocal(),
      detalles.asistenciasLocal()
    );
    List<GolCargado> golesVisitante = ConstructorDetalles.goles(
      plantelVisitante,
      detalles.golesVisitante(),
      detalles.asistenciasVisitante()
    );
    if (
      golesLocal.size() > EstadisticasGoles.golesDe(marcadorLocal) ||
      golesVisitante.size() > EstadisticasGoles.golesDe(marcadorVisitante)
    ) {
      return Optional.empty();
    }
    return Optional.of(
      new ResolucionDetalles(
        golesLocal,
        ConstructorDetalles.tarjetas(
          plantelLocal,
          detalles.tiposTarjetasLocal(),
          detalles.tarjetasLocal()
        ),
        golesVisitante,
        ConstructorDetalles.tarjetas(
          plantelVisitante,
          detalles.tiposTarjetasVisitante(),
          detalles.tarjetasVisitante()
        )
      )
    );
  }
}
