package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.EstadisticasGoles;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.FilaEstadistica;
import com.tallerwebi.dominio.FilaPosicion;
import com.tallerwebi.dominio.GolTorneo;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.RepositorioEncuentroTorneo;
import com.tallerwebi.dominio.RepositorioFaseTorneo;
import com.tallerwebi.dominio.RepositorioGolTorneo;
import com.tallerwebi.dominio.RepositorioInscripcionTorneo;
import com.tallerwebi.dominio.ServicioEstadisticasTorneo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioEstadisticasTorneo")
@Transactional
public class ServicioEstadisticasTorneoImpl implements ServicioEstadisticasTorneo {

  private static final String JUGADO = "JUGADO";

  private final RepositorioGolTorneo repoGol;
  private final RepositorioEncuentroTorneo repoEncuentro;
  private final RepositorioInscripcionTorneo repoInscripcion;
  private final RepositorioFaseTorneo repoFase;

  @Autowired
  public ServicioEstadisticasTorneoImpl(
    RepositorioGolTorneo repoGol,
    RepositorioEncuentroTorneo repoEncuentro,
    RepositorioInscripcionTorneo repoInscripcion,
    RepositorioFaseTorneo repoFase
  ) {
    this.repoGol = repoGol;
    this.repoEncuentro = repoEncuentro;
    this.repoInscripcion = repoInscripcion;
    this.repoFase = repoFase;
  }

  @Override
  public boolean registrarGol(
    Long encuentroId,
    Long inscripcionId,
    String goleador,
    String asistidor
  ) {
    String autor = EstadisticasGoles.normalizar(goleador);
    String pase = EstadisticasGoles.normalizar(asistidor);
    if (
      encuentroId == null || inscripcionId == null || !EstadisticasGoles.nombresValidos(autor, pase)
    ) {
      return false;
    }
    EncuentroTorneo encuentro = repoEncuentro.buscarPorId(encuentroId);
    if (encuentro == null || !JUGADO.equals(encuentro.getEstado())) {
      return false;
    }
    InscripcionTorneo participante = participante(encuentro, inscripcionId);
    if (participante == null) {
      return false;
    }
    int permitidos = EstadisticasGoles.golesDe(
      esParticipanteA(encuentro, inscripcionId) ? encuentro.getGolesA() : encuentro.getGolesB()
    );
    if (repoGol.contarPor(encuentroId, inscripcionId) >= permitidos) {
      return false;
    }
    GolTorneo gol = EstadisticasGoles.completar(new GolTorneo(), autor, pase);
    gol.setEncuentro(encuentro);
    gol.setInscripcion(participante);
    repoGol.guardar(gol);
    return true;
  }

  @Override
  public List<FilaEstadistica> goleadores(Long torneoId) {
    return EstadisticasGoles.goleadores(golesDe(torneoId));
  }

  @Override
  public List<FilaEstadistica> asistencias(Long torneoId) {
    return EstadisticasGoles.asistencias(golesDe(torneoId));
  }

  @Override
  public Map<Long, List<GolTorneo>> golesPorPartido(Long torneoId) {
    return EstadisticasGoles.porPartido(golesDe(torneoId));
  }

  @Override
  public Map<Long, List<String>> jugadoresPorInscripcion(Long torneoId) {
    if (torneoId == null) {
      return Collections.emptyMap();
    }
    Map<Long, List<String>> nombres = new HashMap<>();
    repoInscripcion
      .listarPorTorneo(torneoId)
      .forEach(insc -> nombres.put(insc.getId(), EstadisticasGoles.nombresDe(insc.getEquipo())));
    return nombres;
  }

  @Override
  public List<FilaPosicion> golesPorEquipo(Long torneoId) {
    if (torneoId == null) {
      return Collections.emptyList();
    }
    Map<Long, FilaPosicion> filas = new LinkedHashMap<>();
    for (InscripcionTorneo insc : repoInscripcion.listarPorTorneo(torneoId)) {
      filas.put(insc.getId(), new FilaPosicion(insc.getId(), insc.getNombreVisible()));
    }
    for (FaseTorneo fase : repoFase.listarPorTorneo(torneoId)) {
      fase.getEncuentros().forEach(encuentro -> computar(filas, encuentro));
    }
    List<FilaPosicion> tabla = new ArrayList<>(filas.values());
    tabla.sort(FilaPosicion.ORDEN);
    return tabla;
  }

  private static void computar(Map<Long, FilaPosicion> filas, EncuentroTorneo encuentro) {
    if (
      !JUGADO.equals(encuentro.getEstado()) ||
      encuentro.getParticipanteA() == null ||
      encuentro.getParticipanteB() == null
    ) {
      return;
    }
    FilaPosicion filaA = filas.get(encuentro.getParticipanteA().getId());
    FilaPosicion filaB = filas.get(encuentro.getParticipanteB().getId());
    if (filaA == null || filaB == null) {
      return;
    }
    int golesA = EstadisticasGoles.golesDe(encuentro.getGolesA());
    int golesB = EstadisticasGoles.golesDe(encuentro.getGolesB());
    filaA.registrar(golesA, golesB);
    filaB.registrar(golesB, golesA);
  }

  private List<GolTorneo> golesDe(Long torneoId) {
    return torneoId == null ? Collections.emptyList() : repoGol.listarPorTorneo(torneoId);
  }

  private static boolean esParticipanteA(EncuentroTorneo encuentro, Long inscripcionId) {
    return (
      encuentro.getParticipanteA() != null &&
      inscripcionId.equals(encuentro.getParticipanteA().getId())
    );
  }

  private static InscripcionTorneo participante(EncuentroTorneo encuentro, Long inscripcionId) {
    for (InscripcionTorneo candidato : new InscripcionTorneo[] {
      encuentro.getParticipanteA(),
      encuentro.getParticipanteB(),
    }) {
      if (candidato != null && inscripcionId.equals(candidato.getId())) {
        return candidato;
      }
    }
    return null;
  }
}
