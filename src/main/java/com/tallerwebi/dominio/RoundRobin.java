package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;

public final class RoundRobin {

  private static final int PAR = 2;

  private RoundRobin() {}

  public static <T> List<List<Cruce<T>>> soloIda(List<T> equipos) {
    List<List<Cruce<T>>> fechas = new ArrayList<>();
    if (equipos.size() < PAR) {
      return fechas;
    }
    List<T> lista = new ArrayList<>(equipos);
    if (lista.size() % PAR != 0) {
      lista.add(null);
    }
    int total = lista.size();
    for (int ronda = 0; ronda < total - 1; ronda++) {
      fechas.add(fecha(lista, ronda % PAR == 0));
      lista.add(1, lista.remove(total - 1));
    }
    return fechas;
  }

  public static <T> List<List<Cruce<T>>> idaYVuelta(List<T> equipos) {
    List<List<Cruce<T>>> ida = soloIda(equipos);
    List<List<Cruce<T>>> todas = new ArrayList<>(ida);
    for (List<Cruce<T>> fecha : ida) {
      List<Cruce<T>> vuelta = new ArrayList<>();
      for (Cruce<T> cruce : fecha) {
        vuelta.add(cruce.invertido());
      }
      todas.add(vuelta);
    }
    return todas;
  }

  private static <T> List<Cruce<T>> fecha(List<T> lista, boolean localPrimero) {
    int total = lista.size();
    List<Cruce<T>> cruces = new ArrayList<>();
    for (int i = 0; i < total / PAR; i++) {
      T primero = lista.get(i);
      T ultimo = lista.get(total - 1 - i);
      if (primero == null || ultimo == null) {
        continue;
      }
      cruces.add(localPrimero ? new Cruce<>(primero, ultimo) : new Cruce<>(ultimo, primero));
    }
    return cruces;
  }
}
