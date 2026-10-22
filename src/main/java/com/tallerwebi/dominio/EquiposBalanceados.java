package com.tallerwebi.dominio;

import java.util.List;

public class EquiposBalanceados {

  private List<Usuario> equipoA;
  private List<Usuario> equipoB;

  public EquiposBalanceados(List<Usuario> jugadoresA, List<Usuario> jugadoresB) {
    this.equipoA = jugadoresA;
    this.equipoB = jugadoresB;
  }

  public List<Usuario> getEquipoB() {
    return equipoB;
  }

  public List<Usuario> getEquipoA() {
    return equipoA;
  }

  public int getTotalA() {
    return sumarPl(this.equipoA);
  }

  public int getTotalB() {
    return sumarPl(this.equipoB);
  }

  private int sumarPl(List<Usuario> jugadores) {
    int total = 0;
    for (Usuario u : jugadores) {
      total += u.getPerfil().getPl();
    }

    return total;
  }

  public int getDiferencia() {
    return Math.abs(getTotalA() - getTotalB());
  }
}
