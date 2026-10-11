package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.EstadisticasJugador;
import com.tallerwebi.dominio.GolPartido;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioGolPartido;
import com.tallerwebi.dominio.RepositorioPartido;
import com.tallerwebi.dominio.ServicioEstadisticasJugador;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ServicioEstadisticasJugadorImpl implements ServicioEstadisticasJugador {

  private final RepositorioPartido repositorioPartido;
  private final RepositorioGolPartido repositorioGolPartido;

  public ServicioEstadisticasJugadorImpl(
    RepositorioPartido repositorioPartido,
    RepositorioGolPartido repositorioGolPartido
  ) {
    this.repositorioPartido = repositorioPartido;
    this.repositorioGolPartido = repositorioGolPartido;
  }

  @Override
  public EstadisticasJugador obtenerEstadisticasDelUsuario(Long usuarioId) {
    List<Partido> partidosDelUsuario = repositorioPartido.listarPartidosDe(usuarioId);
    int partidosJugados = 0;
    int golesDelUsuario = 0;
    int asistenciasDelUsuario = 0;
    int partidosGanados = 0;
    int partidosPerdidos = 0;
    int partidosEmpatados = 0;

    for (Partido partido : partidosDelUsuario) {
      if (partido.fueJugado()) {
        partidosJugados++;
        List<GolPartido> golesDelPartido = repositorioGolPartido.listarPorPartido(partido.getId());
        for (GolPartido gol : golesDelPartido) {
          Usuario goleador = gol.getUsuarioGoleador();
          if (goleador != null && goleador.getId().equals(usuarioId)) {
            golesDelUsuario++;
          }
          Usuario asistente = gol.getUsuarioAsistente();
          if (asistente != null && asistente.getId().equals(usuarioId)) {
            asistenciasDelUsuario++;
          }
        }
      }
    }

    partidosGanados += calcularPartidosGanados(partidosDelUsuario, usuarioId);
    partidosPerdidos += calcularPartidosPerdidos(partidosDelUsuario, usuarioId);
    partidosEmpatados += calcularPartidosEmpatados(partidosDelUsuario, usuarioId);
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setPartidosJugados(partidosJugados);
    estadisticas.setGoles(golesDelUsuario);
    estadisticas.setAsistencias(asistenciasDelUsuario);
    estadisticas.setPartidosGanados(partidosGanados);
    estadisticas.setPartidosPerdidos(partidosPerdidos);
    estadisticas.setPartidosEmpatados(partidosEmpatados);
    return estadisticas;
  }

  private boolean perteneceAEquipo(Set<Usuario> equipo, Long usuarioId) {
    return (
      equipo != null &&
      equipo
        .stream()
        .anyMatch(jugador ->
          jugador != null && jugador.getId() != null && jugador.getId().equals(usuarioId)
        )
    );
  }

  private int calcularPartidosGanados(List<Partido> partidosDelUsuario, Long usuarioId) {
    int partidosGanados = 0;
    for (Partido partido : partidosDelUsuario) {
      if (partido.fueJugado()) {
        boolean jugadorEnEquipoA = perteneceAEquipo(partido.getEquipoA(), usuarioId);
        boolean jugadorEnEquipoB = perteneceAEquipo(partido.getEquipoB(), usuarioId);
        int golesA = partido.getGolesEquipoA();
        int golesB = partido.getGolesEquipoB();
        if ((jugadorEnEquipoA && golesA > golesB) || (jugadorEnEquipoB && golesB > golesA)) {
          partidosGanados++;
        }
      }
    }
    return partidosGanados;
  }

  private int calcularPartidosPerdidos(List<Partido> partidosDelUsuario, Long usuarioId) {
    int partidosPerdidos = 0;
    for (Partido partido : partidosDelUsuario) {
      if (partido.fueJugado()) {
        boolean jugadorEnEquipoA = perteneceAEquipo(partido.getEquipoA(), usuarioId);
        boolean jugadorEnEquipoB = perteneceAEquipo(partido.getEquipoB(), usuarioId);
        int golesA = partido.getGolesEquipoA();
        int golesB = partido.getGolesEquipoB();
        if ((jugadorEnEquipoA && golesA < golesB) || (jugadorEnEquipoB && golesB < golesA)) {
          partidosPerdidos++;
        }
      }
    }
    return partidosPerdidos;
  }

  private int calcularPartidosEmpatados(List<Partido> partidosDelUsuario, Long usuarioId) {
    int partidosEmpatados = 0;
    for (Partido partido : partidosDelUsuario) {
      if (partido.fueJugado()) {
        boolean jugadorEnEquipoA = perteneceAEquipo(partido.getEquipoA(), usuarioId);
        boolean jugadorEnEquipoB = perteneceAEquipo(partido.getEquipoB(), usuarioId);
        int golesA = partido.getGolesEquipoA();
        int golesB = partido.getGolesEquipoB();
        if ((jugadorEnEquipoA || jugadorEnEquipoB) && golesA == golesB) {
          partidosEmpatados++;
        }
      }
    }
    return partidosEmpatados;
  }
}
