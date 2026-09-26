package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioCompetencia {
  List<Torneo> listarTorneos();
  List<Liga> listarLigas();
  ResultadoInscripcion inscribirATorneo(Usuario usuario, Long torneoId);

  boolean tieneEquipoActivo(Usuario usuario);

  Map<String, List<FilaPosicion>> calcularTablasPorGrupo(Long torneoId);

  InscripcionTorneo obtenerCampeon(Long torneoId);

  List<Equipo> listarEquiposInscriptos(Long torneoId);

  boolean yaInscripto(Usuario usuario, Long torneoId);

  java.util.List<InscripcionTorneo> listarInscripciones(Long torneoId);

  default Torneo buscarTorneoPorId(Long torneoId) {
    return null;
  }

  default java.util.List<FaseTorneo> obtenerFases(Long torneoId) {
    return java.util.Collections.emptyList();
  }

  boolean generarFixtureSiNoExiste(Long torneoId);

  default List<Torneo> listarTorneos(int page, int size) {
    return listarTorneos();
  }

  default List<Liga> listarLigas(int page, int size) {
    return listarLigas();
  }

  default void guardarTorneo(Torneo torneo) {}

  boolean registrarResultadoEncuentro(Long encuentroId, Integer golesA, Integer golesB);

  default void guardarLiga(Liga liga) {}
}
