package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioEquipo {
  void guardar(Equipo equipo);

  void actualizar(Equipo equipo);

  Equipo buscarPorId(Long id);

  int minimoJugadoresEnCompetencias(Long equipoId);

  List<Equipo> listarActivosDe(Long usuarioId);

  List<Equipo> listarCapitaneados(Long usuarioId);

  List<Equipo> listarActivos();
}
