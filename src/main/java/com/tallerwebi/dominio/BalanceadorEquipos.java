package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BalanceadorEquipos {

  public EquiposBalanceados armarEquipos(List<Usuario> jugadores) {
    List<Usuario> ordenados = new ArrayList<>(jugadores);
    ordenados.sort(
      Comparator.comparing((Usuario jugador) -> jugador.getPerfil().getPl()).reversed()
    );

    int totalA = 0;
    int totalB = 0;

    List<Usuario> jugadoresA = new ArrayList<>();
    List<Usuario> jugadoresB = new ArrayList<>();

    int mitad = jugadores.size() / 2;

    for (Usuario jugador : ordenados) {
      if (jugadoresA.size() < mitad && (totalA < totalB || jugadoresB.size() >= mitad)) {
        jugadoresA.add(jugador);
        totalA += jugador.getPerfil().getPl();
      } else {
        jugadoresB.add(jugador);
        totalB += jugador.getPerfil().getPl();
      }
    }

    return new EquiposBalanceados(jugadoresA, jugadoresB);
  }
}
