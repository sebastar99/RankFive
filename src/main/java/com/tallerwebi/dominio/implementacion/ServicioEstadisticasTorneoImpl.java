package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.ConstructorDetalles;
import com.tallerwebi.dominio.ConstructorDetalles.GolCargado;
import com.tallerwebi.dominio.ConstructorDetalles.TarjetaCargada;
import com.tallerwebi.dominio.DetallesEncuentro;
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
import com.tallerwebi.dominio.RepositorioTarjetaTorneo;
import com.tallerwebi.dominio.ResolucionDetalles;
import com.tallerwebi.dominio.ServicioEstadisticasTorneo;
import com.tallerwebi.dominio.TarjetaTorneo;
import com.tallerwebi.dominio.Usuario;
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
  private final RepositorioTarjetaTorneo repoTarjeta;

  @Autowired
  public ServicioEstadisticasTorneoImpl(
    RepositorioGolTorneo repoGol,
    RepositorioEncuentroTorneo repoEncuentro,
    RepositorioInscripcionTorneo repoInscripcion,
    RepositorioFaseTorneo repoFase,
    RepositorioTarjetaTorneo repoTarjeta
  ) {
    this.repoTarjeta = repoTarjeta;
    this.repoGol = repoGol;
    this.repoEncuentro = repoEncuentro;
    this.repoInscripcion = repoInscripcion;
    this.repoFase = repoFase;
  }

  @Override
  public boolean registrarDetalles(Long encuentroId, DetallesEncuentro detalles) {
    EncuentroTorneo encuentro = encuentroId == null ? null : repoEncuentro.buscarPorId(encuentroId);
    if (
      encuentro == null ||
      !JUGADO.equals(encuentro.getEstado()) ||
      encuentro.getParticipanteA() == null ||
      encuentro.getParticipanteB() == null
    ) {
      return false;
    }
    InscripcionTorneo local = encuentro.getParticipanteA();
    InscripcionTorneo visitante = encuentro.getParticipanteB();
    var resolucion = ResolucionDetalles.de(
      detalles,
      local.getEquipo(),
      visitante.getEquipo(),
      encuentro.getGolesA(),
      encuentro.getGolesB()
    );
    if (resolucion.isEmpty()) {
      return false;
    }
    repoGol.borrarPorEncuentro(encuentroId);
    repoTarjeta.borrarPorEncuentro(encuentroId);
    guardar(encuentro, local, resolucion.get().golesLocal(), resolucion.get().tarjetasLocal());
    guardar(
      encuentro,
      visitante,
      resolucion.get().golesVisitante(),
      resolucion.get().tarjetasVisitante()
    );
    return true;
  }

  private void guardar(
    EncuentroTorneo encuentro,
    InscripcionTorneo inscripcion,
    List<GolCargado> goles,
    List<TarjetaCargada> tarjetas
  ) {
    for (GolCargado cargado : goles) {
      GolTorneo gol = EstadisticasGoles.completar(
        new GolTorneo(),
        cargado.goleador(),
        cargado.asistidor()
      );
      gol.setEncuentro(encuentro);
      gol.setInscripcion(inscripcion);
      repoGol.guardar(gol);
    }
    for (TarjetaCargada cargada : tarjetas) {
      TarjetaTorneo tarjeta = new TarjetaTorneo();
      tarjeta.setEncuentro(encuentro);
      tarjeta.setInscripcion(inscripcion);
      tarjeta.setUsuarioAmonestado(cargada.jugador());
      tarjeta.setJugador(ConstructorDetalles.nombreVisible(cargada.jugador()));
      tarjeta.setTipo(cargada.tipo());
      repoTarjeta.guardar(tarjeta);
    }
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
  public Map<Long, List<TarjetaTorneo>> tarjetasPorPartido(Long torneoId) {
    Map<Long, List<TarjetaTorneo>> agrupadas = new HashMap<>();
    if (torneoId != null) {
      for (TarjetaTorneo tarjeta : repoTarjeta.listarPorTorneo(torneoId)) {
        agrupadas.computeIfAbsent(tarjeta.getPartidoId(), id -> new ArrayList<>()).add(tarjeta);
      }
    }
    return agrupadas;
  }

  @Override
  public Map<Long, List<Usuario>> plantelesPorInscripcion(Long torneoId) {
    if (torneoId == null) {
      return Collections.emptyMap();
    }
    Map<Long, List<Usuario>> planteles = new HashMap<>();
    repoInscripcion
      .listarPorTorneo(torneoId)
      .forEach(insc ->
        planteles.put(
          insc.getId(),
          ConstructorDetalles.plantelDe(insc.getEquipo()).stream().toList()
        )
      );
    return planteles;
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
}
