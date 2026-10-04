package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioArbitro {
  void guardar(Arbitro arbitro);

  void actualizar(Arbitro arbitro);

  Arbitro buscarPorId(Long id);

  List<Arbitro> listarTodos();

  void guardarCalificacion(CalificacionArbitro calificacion);

  boolean existeCalificacion(Long usuarioId, Long encuentroId);

  boolean existeCalificacionLiga(Long usuarioId, Long partidoLigaId);

  List<Long> encuentrosCalificadosPor(Long usuarioId);

  List<Long> partidosLigaCalificadosPor(Long usuarioId);
}
