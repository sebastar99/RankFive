package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioPartido;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.Usuario;
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

  @Autowired
  public ServicioPartidoImpl(
    RepositorioPartido repositorioPartido,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioPartido = repositorioPartido;
    this.repositorioUsuario = repositorioUsuario;
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

  private boolean sonEquiposValidos(List<Usuario> equipoA, List<Usuario> equipoB) {
    if (equipoA == null || equipoB == null || equipoA.isEmpty() || equipoB.isEmpty()) {
      return false;
    }
    Set<Long> idsEquipoA = new HashSet<>();
    for (Usuario jugador : equipoA) {
      idsEquipoA.add(jugador.getId());
    }
    for (Usuario jugador : equipoB) {
      if (idsEquipoA.contains(jugador.getId())) {
        return false;
      }
    }
    return true;
  }

  private void ajustarPl(Partido partido, int golesEquipoA, int golesEquipoB) {
    double promedioA = promedioPl(partido.getEquipoA());
    double promedioB = promedioPl(partido.getEquipoB());
    double esperadoA = probabilidadEsperada(promedioA, promedioB);
    double esperadoB = probabilidadEsperada(promedioB, promedioA);
    double resultadoA = resultadoDe(golesEquipoA, golesEquipoB);
    double resultadoB = 1.0 - resultadoA;

    int deltaA = (int) Math.round(FACTOR_K * (resultadoA - esperadoA));
    int deltaB = (int) Math.round(FACTOR_K * (resultadoB - esperadoB));

    aplicarDelta(partido.getEquipoA(), deltaA);
    aplicarDelta(partido.getEquipoB(), deltaB);
  }

  private double promedioPl(Collection<Usuario> equipo) {
    return equipo.stream().mapToInt(jugador -> jugador.getPerfil().getPl()).average().orElse(0);
  }

  private double probabilidadEsperada(double promedioPropio, double promedioRival) {
    return 1.0 / (1.0 + Math.pow(10, (promedioRival - promedioPropio) / 400.0));
  }

  private double resultadoDe(int golesEquipoA, int golesEquipoB) {
    if (golesEquipoA > golesEquipoB) {
      return 1.0;
    }
    if (golesEquipoA < golesEquipoB) {
      return 0.0;
    }
    return 0.5;
  }

  private void aplicarDelta(Collection<Usuario> equipo, int delta) {
    for (Usuario jugador : equipo) {
      jugador.getPerfil().setPl(jugador.getPerfil().getPl() + delta);
      repositorioUsuario.modificar(jugador);
    }
  }
}
