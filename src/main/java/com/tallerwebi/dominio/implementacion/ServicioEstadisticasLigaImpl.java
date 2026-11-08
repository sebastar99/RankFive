package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.ConstructorDetalles;
import com.tallerwebi.dominio.ConstructorDetalles.GolCargado;
import com.tallerwebi.dominio.ConstructorDetalles.TarjetaCargada;
import com.tallerwebi.dominio.DetallesEncuentro;
import com.tallerwebi.dominio.EstadisticasGoles;
import com.tallerwebi.dominio.FilaEstadistica;
import com.tallerwebi.dominio.GolLiga;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioGolLiga;
import com.tallerwebi.dominio.RepositorioInscripcionLiga;
import com.tallerwebi.dominio.RepositorioPartidoLiga;
import com.tallerwebi.dominio.RepositorioTarjetaLiga;
import com.tallerwebi.dominio.ResolucionDetalles;
import com.tallerwebi.dominio.ServicioEstadisticasLiga;
import com.tallerwebi.dominio.TarjetaLiga;
import com.tallerwebi.dominio.Usuario;
import java.util.ArrayList;
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
  private final RepositorioTarjetaLiga repoTarjeta;

  @Autowired
  public ServicioEstadisticasLigaImpl(
    RepositorioGolLiga repoGol,
    RepositorioPartidoLiga repoPartido,
    RepositorioInscripcionLiga repoInscripcion,
    RepositorioTarjetaLiga repoTarjeta
  ) {
    this.repoGol = repoGol;
    this.repoPartido = repoPartido;
    this.repoInscripcion = repoInscripcion;
    this.repoTarjeta = repoTarjeta;
  }

  @Override
  public boolean registrarDetalles(Long partidoId, DetallesEncuentro detalles) {
    PartidoLiga partido = partidoId == null ? null : repoPartido.buscarPorId(partidoId);
    if (partido == null || !partido.estaJugado()) {
      return false;
    }
    InscripcionLiga local = partido.getLocal();
    InscripcionLiga visitante = partido.getVisitante();
    var resolucion = ResolucionDetalles.de(
      detalles,
      local.getEquipo(),
      visitante.getEquipo(),
      partido.getGolesLocal(),
      partido.getGolesVisitante()
    );
    if (resolucion.isEmpty()) {
      return false;
    }
    repoGol.borrarPorPartido(partidoId);
    repoTarjeta.borrarPorPartido(partidoId);
    guardar(partido, local, resolucion.get().golesLocal(), resolucion.get().tarjetasLocal());
    guardar(
      partido,
      visitante,
      resolucion.get().golesVisitante(),
      resolucion.get().tarjetasVisitante()
    );
    return true;
  }

  private void guardar(
    PartidoLiga partido,
    InscripcionLiga inscripcion,
    List<GolCargado> goles,
    List<TarjetaCargada> tarjetas
  ) {
    for (GolCargado cargado : goles) {
      GolLiga gol = EstadisticasGoles.completar(
        new GolLiga(),
        cargado.goleador(),
        cargado.asistidor()
      );
      gol.setPartido(partido);
      gol.setInscripcion(inscripcion);
      repoGol.guardar(gol);
    }
    for (TarjetaCargada cargada : tarjetas) {
      TarjetaLiga tarjeta = new TarjetaLiga();
      tarjeta.setPartido(partido);
      tarjeta.setInscripcion(inscripcion);
      tarjeta.setUsuarioAmonestado(cargada.jugador());
      tarjeta.setJugador(ConstructorDetalles.nombreVisible(cargada.jugador()));
      tarjeta.setTipo(cargada.tipo());
      repoTarjeta.guardar(tarjeta);
    }
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
  public Map<Long, List<TarjetaLiga>> tarjetasPorPartido(Long ligaId) {
    Map<Long, List<TarjetaLiga>> agrupadas = new HashMap<>();
    if (ligaId != null) {
      for (TarjetaLiga tarjeta : repoTarjeta.listarPorLiga(ligaId)) {
        agrupadas.computeIfAbsent(tarjeta.getPartidoId(), id -> new ArrayList<>()).add(tarjeta);
      }
    }
    return agrupadas;
  }

  @Override
  public Map<Long, List<Usuario>> plantelesPorInscripcion(Long ligaId) {
    Map<Long, List<Usuario>> planteles = new HashMap<>();
    if (ligaId == null) {
      return planteles;
    }
    for (InscripcionLiga insc : repoInscripcion.listarPorLiga(ligaId)) {
      planteles.put(
        insc.getId(),
        ConstructorDetalles.plantelDe(insc.getEquipo()).stream().toList()
      );
    }
    return planteles;
  }

  private List<GolLiga> goles(Long ligaId) {
    return ligaId == null ? Collections.emptyList() : repoGol.listarPorLiga(ligaId);
  }
}
