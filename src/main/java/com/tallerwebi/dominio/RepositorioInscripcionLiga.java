package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioInscripcionLiga {
  void guardar(InscripcionLiga inscripcion);

  boolean existePara(Long usuarioId, Long ligaId);

  List<InscripcionLiga> listarPorLiga(Long ligaId);

  List<Equipo> listarEquipos(Long ligaId);
}
