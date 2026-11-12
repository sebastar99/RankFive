package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EstadisticasPartido {

  private final Long id;
  private final LocalDateTime fecha;
  private final Integer golesEquipoA;
  private final Integer golesEquipoB;
  private final List<Fila> equipoA;
  private final List<Fila> equipoB;

  public boolean isJugado() {
    return golesEquipoA != null && golesEquipoB != null;
  }

  @Getter
  @RequiredArgsConstructor
  public static class Fila {

    private final String nombre;
    private final long goles;
    private final long asistencias;
    private final long amarillas;
    private final long rojas;
  }
}
