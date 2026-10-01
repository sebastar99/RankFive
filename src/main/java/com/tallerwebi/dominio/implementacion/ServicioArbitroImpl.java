package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Arbitro;
import com.tallerwebi.dominio.CalificacionArbitro;
import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.RepositorioArbitro;
import com.tallerwebi.dominio.RepositorioEncuentroTorneo;
import com.tallerwebi.dominio.ServicioArbitro;
import com.tallerwebi.dominio.Usuario;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioArbitro")
@Transactional
public class ServicioArbitroImpl implements ServicioArbitro {

  private static final String ESTADO_JUGADO = "JUGADO";
  private static final String ESTADO_PENDIENTE = "PENDIENTE";

  private final RepositorioArbitro repoArbitro;
  private final RepositorioEncuentroTorneo repoEncuentro;

  @Autowired
  public ServicioArbitroImpl(
    RepositorioArbitro repoArbitro,
    RepositorioEncuentroTorneo repoEncuentro
  ) {
    this.repoArbitro = repoArbitro;
    this.repoEncuentro = repoEncuentro;
  }

  @Override
  public List<Arbitro> listarRanking() {
    List<Arbitro> arbitros = new ArrayList<>(repoArbitro.listarTodos());
    arbitros.sort(
      Comparator
        .comparingDouble(Arbitro::getPromedio)
        .reversed()
        .thenComparing(Arbitro::getPl, Comparator.nullsLast(Comparator.reverseOrder()))
    );
    return arbitros;
  }

  @Override
  public void designar(List<EncuentroTorneo> encuentros) {
    if (encuentros == null || encuentros.isEmpty()) {
      return;
    }
    List<Arbitro> ranking = listarRanking();
    if (ranking.isEmpty()) {
      return;
    }
    int indice = 0;
    for (EncuentroTorneo encuentro : encuentros) {
      if (!ESTADO_PENDIENTE.equals(encuentro.getEstado())) {
        continue;
      }
      encuentro.setArbitro(ranking.get(indice % ranking.size()));
      indice++;
    }
  }

  @Override
  public boolean calificar(Usuario usuario, Long encuentroId, Integer puntaje) {
    if (!datosValidos(usuario, encuentroId, puntaje)) {
      return false;
    }
    EncuentroTorneo encuentro = repoEncuentro.buscarPorId(encuentroId);
    if (!puedeCalificar(usuario, encuentro)) {
      return false;
    }
    Arbitro arbitro = encuentro.getArbitro();
    arbitro.calificar(puntaje);
    repoArbitro.actualizar(arbitro);

    CalificacionArbitro calificacion = new CalificacionArbitro();
    calificacion.setArbitro(arbitro);
    calificacion.setUsuario(usuario);
    calificacion.setEncuentro(encuentro);
    calificacion.setPuntaje(puntaje);
    repoArbitro.guardarCalificacion(calificacion);
    return true;
  }

  private static boolean datosValidos(Usuario usuario, Long encuentroId, Integer puntaje) {
    if (usuario == null || usuario.getId() == null || encuentroId == null || puntaje == null) {
      return false;
    }
    return puntaje >= Arbitro.PUNTAJE_MINIMO && puntaje <= Arbitro.PUNTAJE_MAXIMO;
  }

  private boolean puedeCalificar(Usuario usuario, EncuentroTorneo encuentro) {
    if (encuentro == null || encuentro.getArbitro() == null) {
      return false;
    }
    if (!ESTADO_JUGADO.equals(encuentro.getEstado()) || !participo(usuario, encuentro)) {
      return false;
    }
    return !repoArbitro.existeCalificacion(usuario.getId(), encuentro.getId());
  }

  private static boolean participo(Usuario usuario, EncuentroTorneo encuentro) {
    return (
      perteneceA(usuario, encuentro.getParticipanteA()) ||
      perteneceA(usuario, encuentro.getParticipanteB())
    );
  }

  private static boolean perteneceA(Usuario usuario, InscripcionTorneo inscripcion) {
    if (inscripcion == null) {
      return false;
    }
    Long id = usuario.getId();
    if (inscripcion.getUsuario() != null && id.equals(inscripcion.getUsuario().getId())) {
      return true;
    }
    if (inscripcion.getEquipo() == null) {
      return false;
    }
    return inscripcion
      .getEquipo()
      .getJugadores()
      .stream()
      .anyMatch(jugador -> id.equals(jugador.getId()));
  }
}
