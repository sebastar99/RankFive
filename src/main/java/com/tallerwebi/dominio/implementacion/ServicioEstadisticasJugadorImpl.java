package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.EstadisticasJugador;
import com.tallerwebi.dominio.GolPartido;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioGolPartido;
import com.tallerwebi.dominio.RepositorioPartido;
import com.tallerwebi.dominio.RepositorioTarjetaPartido;
import com.tallerwebi.dominio.ServicioEstadisticasJugador;
import com.tallerwebi.dominio.TarjetaPartido;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.TipoTarjeta;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ServicioEstadisticasJugadorImpl implements ServicioEstadisticasJugador {

  private final RepositorioPartido repositorioPartido;
  private final RepositorioGolPartido repositorioGolPartido;
  private final RepositorioTarjetaPartido repositorioTarjetaPartido;

  public ServicioEstadisticasJugadorImpl(
    RepositorioPartido repositorioPartido,
    RepositorioGolPartido repositorioGolPartido,
    RepositorioTarjetaPartido repositorioTarjetaPartido
  ) {
    this.repositorioPartido = repositorioPartido;
    this.repositorioGolPartido = repositorioGolPartido;
    this.repositorioTarjetaPartido = repositorioTarjetaPartido;
  }

  @Override
  public EstadisticasJugador obtenerEstadisticasDelUsuario(Long usuarioId) {
    List<Partido> partidosDelUsuario = repositorioPartido.listarPartidosDe(usuarioId);
    int partidosJugados = 0;
    int golesDelUsuario = 0;
    int asistenciasDelUsuario = 0;
    int tarjetasAmarillas = 0;
    int tarjetasRojas = 0;
    int partidosGanados = 0;
    int partidosPerdidos = 0;
    int partidosEmpatados = 0;

    for (Partido partido : partidosDelUsuario) {
      if (partido.fueJugado()) {
        partidosJugados++;
        List<GolPartido> golesDelPartido = repositorioGolPartido.listarPorPartido(partido.getId());
        golesDelUsuario += contarGolesDe(golesDelPartido, usuarioId);
        asistenciasDelUsuario += contarAsistenciasDe(golesDelPartido, usuarioId);
        List<TarjetaPartido> tarjetasDelPartido =
          repositorioTarjetaPartido.listarTarjetasPorPartido(partido.getId());
        tarjetasAmarillas += contarTarjetasDe(tarjetasDelPartido, usuarioId, TipoTarjeta.AMARILLA);
        tarjetasRojas += contarTarjetasDe(tarjetasDelPartido, usuarioId, TipoTarjeta.ROJA);
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
    estadisticas.setTarjetasAmarillas(tarjetasAmarillas);
    estadisticas.setTarjetasRojas(tarjetasRojas);
    return estadisticas;
  }

  private int contarGolesDe(List<GolPartido> goles, Long usuarioId) {
    int total = 0;
    for (GolPartido gol : goles) {
      Usuario goleador = gol.getUsuarioGoleador();
      if (goleador != null && goleador.getId().equals(usuarioId)) {
        total++;
      }
    }
    return total;
  }

  private int contarAsistenciasDe(List<GolPartido> goles, Long usuarioId) {
    int total = 0;
    for (GolPartido gol : goles) {
      Usuario asistente = gol.getUsuarioAsistente();
      if (asistente != null && asistente.getId().equals(usuarioId)) {
        total++;
      }
    }
    return total;
  }

  private int contarTarjetasDe(List<TarjetaPartido> tarjetas, Long usuarioId, TipoTarjeta tipo) {
    int total = 0;
    for (TarjetaPartido tarjeta : tarjetas) {
      Usuario amonestado = tarjeta.getUsuarioAmonestado();
      if (amonestado != null && amonestado.getId().equals(usuarioId) && tarjeta.getTipo() == tipo) {
        total++;
      }
    }
    return total;
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
