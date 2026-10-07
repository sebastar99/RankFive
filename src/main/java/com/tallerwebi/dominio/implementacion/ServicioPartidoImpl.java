package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.GolPartido;
import com.tallerwebi.dominio.NotificacionPartido;
import com.tallerwebi.dominio.NotificacionPl;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioGolPartido;
import com.tallerwebi.dominio.RepositorioNotificacionPartido;
import com.tallerwebi.dominio.RepositorioNotificacionPl;
import com.tallerwebi.dominio.RepositorioPartido;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.SoportePartido;
import com.tallerwebi.dominio.Usuario;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioPartido")
@Transactional
public class ServicioPartidoImpl implements ServicioPartido {

  private static final int FACTOR_K = 32;

  private final RepositorioPartido repositorioPartido;
  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioNotificacionPl repositorioNotificacionPl;
  private final RepositorioNotificacionPartido repositorioNotificacionPartido;
  private final RepositorioGolPartido repositorioGolPartido;
  private final SoportePartido soportePartido;

  @Autowired
  public ServicioPartidoImpl(
    RepositorioPartido repositorioPartido,
    RepositorioUsuario repositorioUsuario,
    RepositorioNotificacionPl repositorioNotificacionPl,
    RepositorioNotificacionPartido repositorioNotificacionPartido,
    RepositorioGolPartido repositorioGolPartido,
    SoportePartido soportePartido
  ) {
    this.repositorioPartido = repositorioPartido;
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioNotificacionPl = repositorioNotificacionPl;
    this.repositorioNotificacionPartido = repositorioNotificacionPartido;
    this.repositorioGolPartido = repositorioGolPartido;
    this.soportePartido = soportePartido;
  }

  public ServicioPartidoImpl(
    RepositorioPartido repositorioPartido,
    RepositorioUsuario repositorioUsuario,
    RepositorioNotificacionPl repositorioNotificacionPl
  ) {
    this(repositorioPartido, repositorioUsuario, repositorioNotificacionPl, null, null, null);
  }

  @Override
  public Partido crearPartido(List<Usuario> equipoA, List<Usuario> equipoB) {
    if (!sonEquiposValidos(equipoA, equipoB)) {
      return null;
    }
    Partido partido = new Partido();
    partido.setEquipoA(new HashSet<>(equipoA));
    partido.setEquipoB(new HashSet<>(equipoB));
    repositorioPartido.guardar(partido);
    notificarParticipantes(partido);
    return partido;
  }

  @Override
  public Partido crearPartido(List<Usuario> equipoA, List<Usuario> equipoB, LocalDateTime fecha) {
    Partido partido = crearPartido(equipoA, equipoB);
    if (partido != null && fecha != null) {
      partido.setFecha(fecha);
      repositorioPartido.actualizar(partido);
    }
    return partido;
  }

  @Override
  public boolean registrarResultado(Long partidoId, int golesEquipoA, int golesEquipoB) {
    if (partidoId == null || golesEquipoA < 0 || golesEquipoB < 0) {
      return false;
    }
    Partido partido = repositorioPartido.buscarPorId(partidoId);
    if (partido == null || partido.fueJugado()) {
      return false;
    }

    partido.registrarResultado(golesEquipoA, golesEquipoB);
    ajustarPl(partido, golesEquipoA, golesEquipoB);
    repositorioPartido.actualizar(partido);
    return true;
  }

  @Override
  public List<Partido> listarPartidosDe(Usuario usuario) {
    if (usuario == null || usuario.getId() == null) {
      return Collections.emptyList();
    }
    return repositorioPartido.listarPartidosDe(usuario.getId());
  }

  @Override
  public Usuario buscarPorEmail(String email) {
    return repositorioUsuario.buscar(email);
  }

  @Override
  public List<NotificacionPl> listarNotificacionesNoLeidas(Usuario usuario) {
    if (usuario == null || usuario.getId() == null) {
      return Collections.emptyList();
    }
    return repositorioNotificacionPl.listarNoLeidasDe(usuario.getId());
  }

  @Override
  public List<NotificacionPartido> listarNotificacionesPartidoNoLeidas(Usuario usuario) {
    if (usuario == null || usuario.getId() == null) {
      return Collections.emptyList();
    }
    return repositorioNotificacionPartido.listarNoLeidasDe(usuario.getId());
  }

  @Override
  public void marcarNotificacionesLeidas(Usuario usuario) {
    if (usuario == null || usuario.getId() == null) {
      return;
    }
    repositorioNotificacionPl.marcarLeidasDe(usuario.getId());
    repositorioNotificacionPartido.marcarLeidasDe(usuario.getId());
  }

  @Override
  public List<NotificacionPl> listarNotificacionesDe(Usuario usuario) {
    if (usuario == null || usuario.getId() == null) {
      return Collections.emptyList();
    }
    return repositorioNotificacionPl.listarDe(usuario.getId());
  }

  @Override
  public boolean registrarDetalles(
    Long partidoId,
    List<Long> golesAIds,
    List<Long> asistenciasAIds,
    List<Long> golesBIds,
    List<Long> asistenciasBIds
  ) {
    if (partidoId == null || repositorioGolPartido == null) return false;
    Partido partido = repositorioPartido.buscarPorId(partidoId);
    if (partido == null) return false;
    repositorioGolPartido.borrarPorPartido(partidoId);
    if (soportePartido == null) {
      // Sin soporte, no registramos filas para evitar duplicar lógica compleja
      return true;
    }
    List<GolPartido> goles = soportePartido.construirGoles(
      partido,
      golesAIds,
      asistenciasAIds,
      golesBIds,
      asistenciasBIds
    );
    for (GolPartido gol : goles) {
      repositorioGolPartido.guardar(gol);
    }
    return true;
  }

  private boolean sonEquiposValidos(List<Usuario> equipoA, List<Usuario> equipoB) {
    return soportePartido == null
      ? (equipoA != null &&
        equipoB != null &&
        !equipoA.isEmpty() &&
        !equipoB.isEmpty() &&
        java.util.Collections.disjoint(
          equipoA.stream().map(Usuario::getId).toList(),
          equipoB.stream().map(Usuario::getId).toList()
        ))
      : soportePartido.equiposSinSolapamiento(equipoA, equipoB);
  }

  private void ajustarPl(Partido partido, int golesEquipoA, int golesEquipoB) {
    int[] deltas = soportePartido == null
      ? new int[] { 0, 0 }
      : soportePartido.calcularDeltasElo(
        partido.getEquipoA(),
        partido.getEquipoB(),
        golesEquipoA,
        golesEquipoB,
        FACTOR_K
      );
    aplicarDelta(partido, partido.getEquipoA(), deltas[0]);
    aplicarDelta(partido, partido.getEquipoB(), deltas[1]);
  }

  private void aplicarDelta(Partido partido, Collection<Usuario> equipo, int delta) {
    for (Usuario jugador : equipo) {
      jugador.getPerfil().setPl(jugador.getPerfil().getPl() + delta);
      repositorioUsuario.modificar(jugador);
      NotificacionPl notificacion = new NotificacionPl();
      notificacion.setUsuario(jugador);
      notificacion.setPartido(partido);
      notificacion.setDelta(delta);
      repositorioNotificacionPl.guardar(notificacion);
    }
  }

  private void notificarParticipantes(Partido partido) {
    if (repositorioNotificacionPartido == null) {
      return;
    }
    Set<Usuario> participantes = new HashSet<>(partido.getEquipoA());
    participantes.addAll(partido.getEquipoB());
    for (Usuario jugador : participantes) {
      NotificacionPartido notificacion = new NotificacionPartido();
      notificacion.setUsuario(jugador);
      notificacion.setPartido(partido);
      repositorioNotificacionPartido.guardar(notificacion);
    }
  }
}
