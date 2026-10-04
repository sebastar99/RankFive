package com.tallerwebi.dominio.implementacion;

import static com.tallerwebi.dominio.EstadisticasGoles.golesDe;
import static com.tallerwebi.dominio.EstadisticasGoles.nombresValidos;
import static com.tallerwebi.dominio.EstadisticasGoles.normalizar;

import com.tallerwebi.dominio.EstadisticasGoles;
import com.tallerwebi.dominio.FilaEstadistica;
import com.tallerwebi.dominio.GolLiga;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioGolLiga;
import com.tallerwebi.dominio.RepositorioInscripcionLiga;
import com.tallerwebi.dominio.RepositorioPartidoLiga;
import com.tallerwebi.dominio.ServicioEstadisticasLiga;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioEstadisticasLiga")
@Transactional
public class ServicioEstadisticasLigaImpl implements ServicioEstadisticasLiga {

  private final RepositorioGolLiga repoGol;
  private final RepositorioPartidoLiga repoPartido;
  private final RepositorioInscripcionLiga repoInscripcion;

  @Autowired
  public ServicioEstadisticasLigaImpl(
    RepositorioGolLiga repoGol,
    RepositorioPartidoLiga repoPartido,
    RepositorioInscripcionLiga repoInscripcion
  ) {
    this.repoGol = repoGol;
    this.repoPartido = repoPartido;
    this.repoInscripcion = repoInscripcion;
  }

  @Override
  public boolean registrarGol(
    Long partidoId,
    Long inscripcionId,
    String goleador,
    String asistidor
  ) {
    String nombreGoleador = normalizar(goleador);
    String nombreAsistidor = normalizar(asistidor);
    if (
      partidoId == null || inscripcionId == null || !nombresValidos(nombreGoleador, nombreAsistidor)
    ) {
      return false;
    }
    PartidoLiga partido = repoPartido.buscarPorId(partidoId);
    if (partido == null || !partido.estaJugado()) {
      return false;
    }
    InscripcionLiga equipo = equipoDelPartido(partido, inscripcionId);
    if (
      equipo == null ||
      repoGol.contarPor(partidoId, inscripcionId) >= golesDelEquipo(partido, inscripcionId)
    ) {
      return false;
    }
    GolLiga gol = EstadisticasGoles.completar(new GolLiga(), nombreGoleador, nombreAsistidor);
    gol.setPartido(partido);
    gol.setInscripcion(equipo);
    repoGol.guardar(gol);
    return true;
  }

  @Override
  public List<FilaEstadistica> goleadores(Long ligaId) {
    return EstadisticasGoles.goleadores(goles(ligaId));
  }

  @Override
  public List<FilaEstadistica> asistencias(Long ligaId) {
    return EstadisticasGoles.asistencias(goles(ligaId));
  }

  @Override
  public Map<Long, List<GolLiga>> golesPorPartido(Long ligaId) {
    return EstadisticasGoles.porPartido(goles(ligaId));
  }

  @Override
  public Map<Long, List<String>> jugadoresPorInscripcion(Long ligaId) {
    Map<Long, List<String>> jugadores = new HashMap<>();
    if (ligaId == null) {
      return jugadores;
    }
    for (InscripcionLiga insc : repoInscripcion.listarPorLiga(ligaId)) {
      jugadores.put(insc.getId(), EstadisticasGoles.nombresDe(insc.getEquipo()));
    }
    return jugadores;
  }

  private List<GolLiga> goles(Long ligaId) {
    return ligaId == null ? Collections.emptyList() : repoGol.listarPorLiga(ligaId);
  }

  private static InscripcionLiga equipoDelPartido(PartidoLiga partido, Long inscripcionId) {
    if (partido.getLocal() != null && inscripcionId.equals(partido.getLocal().getId())) {
      return partido.getLocal();
    }
    if (partido.getVisitante() != null && inscripcionId.equals(partido.getVisitante().getId())) {
      return partido.getVisitante();
    }
    return null;
  }

  private static int golesDelEquipo(PartidoLiga partido, Long inscripcionId) {
    boolean esLocal = inscripcionId.equals(partido.getLocal().getId());
    return golesDe(esLocal ? partido.getGolesLocal() : partido.getGolesVisitante());
  }
}
