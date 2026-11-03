package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioLiga {
  Liga buscarPorId(Long ligaId);

  default ResultadoInscripcion inscribir(Usuario usuario, Long ligaId) {
    return inscribir(usuario, ligaId, null);
  }

  ResultadoInscripcion inscribir(Usuario usuario, Long ligaId, Long equipoId);

  List<Equipo> listarEquiposCompatibles(Usuario usuario, Integer formato);

  boolean yaInscripto(Usuario usuario, Long ligaId);

  List<InscripcionLiga> listarInscripciones(Long ligaId);

  boolean generarFixtureSiNoExiste(Long ligaId);

  Map<Integer, List<PartidoLiga>> obtenerFechas(Long ligaId);

  boolean registrarResultado(Long partidoId, Integer golesLocal, Integer golesVisitante);

  List<FilaPosicion> calcularTabla(Long ligaId);

  List<Equipo> listarEquiposInscriptos(Long ligaId);
}
