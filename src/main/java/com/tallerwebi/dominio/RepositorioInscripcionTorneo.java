package com.tallerwebi.dominio;

@FunctionalInterface
public interface RepositorioInscripcionTorneo {
  void guardar(InscripcionTorneo inscripcion);

  default boolean existePara(Long usuarioId, Long torneoId) {
    return false;
  }

  default java.util.List<InscripcionTorneo> listarPorTorneo(Long torneoId) {
    return java.util.Collections.emptyList();
  }

  default java.util.List<Equipo> listarEquipos(Long torneoId) {
    return java.util.Collections.emptyList();
  }
}
