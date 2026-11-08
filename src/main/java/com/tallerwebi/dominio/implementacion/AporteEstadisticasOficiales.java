package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.ConstructorDetalles;
import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadisticasGoles;
import com.tallerwebi.dominio.EstadisticasJugador;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioEncuentroTorneo;
import com.tallerwebi.dominio.RepositorioGolLiga;
import com.tallerwebi.dominio.RepositorioGolTorneo;
import com.tallerwebi.dominio.RepositorioPartidoLiga;
import com.tallerwebi.dominio.RepositorioTarjetaLiga;
import com.tallerwebi.dominio.RepositorioTarjetaTorneo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.TipoTarjeta;
import org.springframework.stereotype.Component;

@Component
public class AporteEstadisticasOficiales {

  private final RepositorioPartidoLiga repoPartidoLiga;
  private final RepositorioEncuentroTorneo repoEncuentro;
  private final RepositorioGolLiga repoGolLiga;
  private final RepositorioGolTorneo repoGolTorneo;
  private final RepositorioTarjetaLiga repoTarjetaLiga;
  private final RepositorioTarjetaTorneo repoTarjetaTorneo;

  public AporteEstadisticasOficiales(
    RepositorioPartidoLiga repoPartidoLiga,
    RepositorioEncuentroTorneo repoEncuentro,
    RepositorioGolLiga repoGolLiga,
    RepositorioGolTorneo repoGolTorneo,
    RepositorioTarjetaLiga repoTarjetaLiga,
    RepositorioTarjetaTorneo repoTarjetaTorneo
  ) {
    this.repoPartidoLiga = repoPartidoLiga;
    this.repoEncuentro = repoEncuentro;
    this.repoGolLiga = repoGolLiga;
    this.repoGolTorneo = repoGolTorneo;
    this.repoTarjetaLiga = repoTarjetaLiga;
    this.repoTarjetaTorneo = repoTarjetaTorneo;
  }

  public void sumar(Long usuarioId, EstadisticasJugador estadisticas) {
    if (usuarioId == null) {
      return;
    }
    estadisticas.setGoles(
      estadisticas.getGoles() +
      (int) (repoGolLiga.contarGolesDe(usuarioId) + repoGolTorneo.contarGolesDe(usuarioId))
    );
    estadisticas.setAsistencias(
      estadisticas.getAsistencias() +
      (int) (repoGolLiga.contarAsistenciasDe(usuarioId) +
        repoGolTorneo.contarAsistenciasDe(usuarioId))
    );
    estadisticas.setTarjetasAmarillas(
      estadisticas.getTarjetasAmarillas() +
      (int) (repoTarjetaLiga.contarDe(usuarioId, TipoTarjeta.AMARILLA) +
        repoTarjetaTorneo.contarDe(usuarioId, TipoTarjeta.AMARILLA))
    );
    estadisticas.setTarjetasRojas(
      estadisticas.getTarjetasRojas() +
      (int) (repoTarjetaLiga.contarDe(usuarioId, TipoTarjeta.ROJA) +
        repoTarjetaTorneo.contarDe(usuarioId, TipoTarjeta.ROJA))
    );
    sumarResultados(usuarioId, estadisticas);
  }

  private void sumarResultados(Long usuarioId, EstadisticasJugador estadisticas) {
    for (PartidoLiga partido : repoPartidoLiga.listarJugados()) {
      if (partido.getLocal() != null && partido.getVisitante() != null) {
        sumarEncuentro(
          estadisticas,
          partido.getLocal().getEquipo(),
          partido.getVisitante().getEquipo(),
          partido.getGolesLocal(),
          partido.getGolesVisitante(),
          usuarioId
        );
      }
    }
    for (EncuentroTorneo encuentro : repoEncuentro.listarJugados()) {
      if (encuentro.getParticipanteA() != null && encuentro.getParticipanteB() != null) {
        sumarEncuentro(
          estadisticas,
          encuentro.getParticipanteA().getEquipo(),
          encuentro.getParticipanteB().getEquipo(),
          encuentro.getGolesA(),
          encuentro.getGolesB(),
          usuarioId
        );
      }
    }
  }

  private static void sumarEncuentro(
    EstadisticasJugador estadisticas,
    Equipo local,
    Equipo visitante,
    Integer golesLocal,
    Integer golesVisitante,
    Long usuarioId
  ) {
    boolean esLocal = juega(local, usuarioId);
    boolean esVisitante = !esLocal && juega(visitante, usuarioId);
    if (esLocal || esVisitante) {
      int propios = EstadisticasGoles.golesDe(esLocal ? golesLocal : golesVisitante);
      int rivales = EstadisticasGoles.golesDe(esLocal ? golesVisitante : golesLocal);
      registrar(estadisticas, propios, rivales);
    }
  }

  private static boolean juega(Equipo equipo, Long usuarioId) {
    for (Usuario jugador : ConstructorDetalles.plantelDe(equipo)) {
      if (jugador != null && usuarioId.equals(jugador.getId())) {
        return true;
      }
    }
    return false;
  }

  private static void registrar(EstadisticasJugador estadisticas, int propios, int rivales) {
    estadisticas.setPartidosJugados(estadisticas.getPartidosJugados() + 1);
    if (propios > rivales) {
      estadisticas.setPartidosGanados(estadisticas.getPartidosGanados() + 1);
    } else if (propios < rivales) {
      estadisticas.setPartidosPerdidos(estadisticas.getPartidosPerdidos() + 1);
    } else {
      estadisticas.setPartidosEmpatados(estadisticas.getPartidosEmpatados() + 1);
    }
  }
}
