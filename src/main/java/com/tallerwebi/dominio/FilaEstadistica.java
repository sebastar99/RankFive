package com.tallerwebi.dominio;

import java.util.Comparator;
import lombok.Getter;

@Getter
public class FilaEstadistica {

  public static final Comparator<FilaEstadistica> ORDEN = Comparator
    .comparingInt(FilaEstadistica::getCantidad)
    .reversed()
    .thenComparing(FilaEstadistica::getJugador, String.CASE_INSENSITIVE_ORDER);

  private final String jugador;
  private final String equipo;
  private int cantidad;

  public FilaEstadistica(String jugador, String equipo) {
    this.jugador = jugador;
    this.equipo = equipo;
  }

  public void sumar() {
    cantidad++;
  }
}
