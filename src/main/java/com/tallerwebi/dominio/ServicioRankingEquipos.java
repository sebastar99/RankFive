package com.tallerwebi.dominio;

import java.util.List;

@FunctionalInterface
public interface ServicioRankingEquipos {
  List<PosicionEquipo> obtenerRanking(Usuario usuario, int limite);
}
