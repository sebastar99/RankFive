package com.tallerwebi.dominio;

import java.util.Comparator;
import lombok.Getter;

@Getter
public class FilaPosicion {

  public static final Comparator<FilaPosicion> ORDEN = Comparator
    .comparingInt(FilaPosicion::getPuntos)
    .thenComparingInt(FilaPosicion::getDiferencia)
    .thenComparingInt(FilaPosicion::getGolesFavor)
    .reversed();

  private static final int PUNTOS_VICTORIA = 3;

  private final Long participanteId;
  private final String nombreEquipo;
  private int jugados;
  private int ganados;
  private int empatados;
  private int perdidos;
  private int golesFavor;
  private int golesContra;

  public FilaPosicion(Long participanteId, String nombreEquipo) {
    this.participanteId = participanteId;
    this.nombreEquipo = nombreEquipo;
  }

  public void registrar(int propios, int rivales) {
    jugados++;
    golesFavor += propios;
    golesContra += rivales;
    if (propios > rivales) {
      ganados++;
    } else if (propios == rivales) {
      empatados++;
    } else {
      perdidos++;
    }
  }

  public int getDiferencia() {
    return golesFavor - golesContra;
  }

  public int getPuntos() {
    return ganados * PUNTOS_VICTORIA + empatados;
  }
}
