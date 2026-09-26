package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioLiga {
  Liga buscarPorId(Long ligaId);

  ResultadoInscripcion inscribir(Usuario usuario, Long ligaId);

  boolean yaInscripto(Usuario usuario, Long ligaId);

  List<InscripcionLiga> listarInscripciones(Long ligaId);

  boolean generarFixtureSiNoExiste(Long ligaId);

  Map<Integer, List<PartidoLiga>> obtenerFechas(Long ligaId);

  boolean registrarResultado(Long partidoId, Integer golesLocal, Integer golesVisitante);

  List<FilaPosicion> calcularTabla(Long ligaId);

  List<Equipo> listarEquiposInscriptos(Long ligaId);
}
