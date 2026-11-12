package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.EstadisticasPartido;
import com.tallerwebi.dominio.GolPartido;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioGolPartido;
import com.tallerwebi.dominio.RepositorioPartido;
import com.tallerwebi.dominio.RepositorioTarjetaPartido;
import com.tallerwebi.dominio.ServicioEstadisticasPartido;
import com.tallerwebi.dominio.TarjetaPartido;
import com.tallerwebi.dominio.Usuario;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicioEstadisticasPartidoImpl implements ServicioEstadisticasPartido {

  private final RepositorioPartido partidos;
  private final RepositorioGolPartido goles;
  private final RepositorioTarjetaPartido tarjetas;

  @Autowired
  public ServicioEstadisticasPartidoImpl(
    RepositorioPartido partidos,
    RepositorioGolPartido goles,
    RepositorioTarjetaPartido tarjetas
  ) {
    this.partidos = partidos;
    this.goles = goles;
    this.tarjetas = tarjetas;
  }

  @Override
  public Optional<EstadisticasPartido> obtener(Long partidoId, Usuario usuario) {
    if (usuario == null || usuario.getId() == null || partidoId == null) {
      return Optional.empty();
    }

    Partido partido = partidos.buscarPorId(partidoId);

    if (partido == null) {
      return Optional.empty();
    }

    boolean participo = Stream
      .concat(partido.getEquipoA().stream(), partido.getEquipoB().stream())
      .anyMatch(jugador -> mismoUsuario(usuario, jugador));

    if (!participo) {
      return Optional.empty();
    }

    List<GolPartido> detalleGoles = partido.fueJugado()
      ? goles.listarPorPartido(partidoId)
      : List.of();

    List<TarjetaPartido> detalleTarjetas = partido.fueJugado()
      ? tarjetas.listarTarjetasPorPartido(partidoId)
      : List.of();

    EstadisticasPartido estadisticas = new EstadisticasPartido(
      partido.getId(),
      partido.getFecha(),
      partido.getGolesEquipoA(),
      partido.getGolesEquipoB(),
      filas(partido.getEquipoA(), detalleGoles, detalleTarjetas),
      filas(partido.getEquipoB(), detalleGoles, detalleTarjetas)
    );

    return Optional.of(estadisticas);
  }

  private List<EstadisticasPartido.Fila> filas(
    Set<Usuario> equipo,
    List<GolPartido> detalleGoles,
    List<TarjetaPartido> detalleTarjetas
  ) {
    return equipo
      .stream()
      .sorted(Comparator.comparing(Usuario::getId))
      .map(jugador ->
        new EstadisticasPartido.Fila(
          nombre(jugador),
          detalleGoles
            .stream()
            .filter(gol -> mismoUsuario(jugador, gol.getUsuarioGoleador()))
            .count(),
          detalleGoles
            .stream()
            .filter(gol -> mismoUsuario(jugador, gol.getUsuarioAsistente()))
            .count(),
          detalleTarjetas
            .stream()
            .filter(tarjeta ->
              mismoUsuario(jugador, tarjeta.getUsuarioAmonestado()) && tarjeta.esAmarilla()
            )
            .count(),
          detalleTarjetas
            .stream()
            .filter(tarjeta ->
              mismoUsuario(jugador, tarjeta.getUsuarioAmonestado()) && tarjeta.esRoja()
            )
            .count()
        )
      )
      .toList();
  }

  private boolean mismoUsuario(Usuario jugador, Usuario otro) {
    return otro != null && jugador.getId().equals(otro.getId());
  }

  private String nombre(Usuario jugador) {
    if (jugador.getPerfil() != null) {
      String nombreCompleto = jugador.getPerfil().getNombreCompleto();

      if (nombreCompleto != null && !nombreCompleto.isBlank()) {
        return nombreCompleto;
      }
    }

    return jugador.getEmail();
  }
}
