package com.tallerwebi.dominio;

import java.util.Comparator;

public class EstadisticaEquipo {

  public static final Comparator<EstadisticaEquipo> ORDEN = (a, b) -> {
    int cmp = Integer.compare(b.getPuntos(), a.getPuntos());
    if (cmp == 0) {
      cmp = Integer.compare(b.getDiferencia(), a.getDiferencia());
    }
    if (cmp == 0) {
      cmp =
        String.CASE_INSENSITIVE_ORDER.compare(a.getEquipo().getNombre(), b.getEquipo().getNombre());
    }
    return cmp;
  };

  private final Equipo equipo;
  private final FilaPosicion fila;
  private int competencias;

  public EstadisticaEquipo(Equipo equipo) {
    this.equipo = equipo;
    this.fila = new FilaPosicion(equipo.getId(), equipo.getNombre());
  }

  public void registrar(int propios, int rivales) {
    fila.registrar(propios, rivales);
  }

  public void sumarCompetencia() {
    competencias++;
  }

  public Equipo getEquipo() {
    return equipo;
  }

  public int getCompetencias() {
    return competencias;
  }

  public int getJugados() {
    return fila.getJugados();
  }

  public int getGanados() {
    return fila.getGanados();
  }

  public int getEmpatados() {
    return fila.getEmpatados();
  }

  public int getPerdidos() {
    return fila.getPerdidos();
  }

  public int getGolesFavor() {
    return fila.getGolesFavor();
  }

  public int getGolesContra() {
    return fila.getGolesContra();
  }

  public int getDiferencia() {
    return fila.getDiferencia();
  }

  public int getPuntos() {
    return fila.getPuntos();
  }

  public RangoEquipo getRango() {
    return equipo.getRango();
  }
}
