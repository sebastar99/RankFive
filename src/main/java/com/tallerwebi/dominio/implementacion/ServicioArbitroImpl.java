package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Arbitro;
import com.tallerwebi.dominio.CalificacionArbitro;
import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioArbitro;
import com.tallerwebi.dominio.RepositorioEncuentroTorneo;
import com.tallerwebi.dominio.RepositorioFaseTorneo;
import com.tallerwebi.dominio.RepositorioPartidoLiga;
import com.tallerwebi.dominio.ServicioArbitro;
import com.tallerwebi.dominio.Usuario;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioArbitro")
@Transactional
public class ServicioArbitroImpl implements ServicioArbitro {

  private static final String ESTADO_JUGADO = "JUGADO";
  private static final String ESTADO_PENDIENTE = "PENDIENTE";
  private static final String ROL_ADMIN = "ADMIN";

  private final RepositorioArbitro repoArbitro;
  private final RepositorioEncuentroTorneo repoEncuentro;
  private final RepositorioPartidoLiga repoPartidoLiga;
  private final RepositorioFaseTorneo repoFase;

  @Autowired
  public ServicioArbitroImpl(
    RepositorioArbitro repoArbitro,
    RepositorioEncuentroTorneo repoEncuentro,
    RepositorioPartidoLiga repoPartidoLiga,
    RepositorioFaseTorneo repoFase
  ) {
    this.repoArbitro = repoArbitro;
    this.repoEncuentro = repoEncuentro;
    this.repoPartidoLiga = repoPartidoLiga;
    this.repoFase = repoFase;
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
    return aplicar(usuario, encuentro.getArbitro(), puntaje, c -> c.setEncuentro(encuentro));
  }

  @Override
  public boolean calificarPartidoLiga(Usuario usuario, Long partidoId, Integer puntaje) {
    if (!datosValidos(usuario, partidoId, puntaje)) {
      return false;
    }
    PartidoLiga partido = repoPartidoLiga.buscarPorId(partidoId);
    if (!puedeCalificarLiga(usuario, partido)) {
      return false;
    }
    if (repoArbitro.existeCalificacionLiga(usuario.getId(), partidoId)) {
      return false;
    }
    return aplicar(usuario, partido.getArbitro(), puntaje, c -> c.setPartidoLiga(partido));
  }

  @Override
  public Set<Long> encuentrosCalificables(Usuario usuario, Long torneoId) {
    if (usuario == null || usuario.getId() == null || torneoId == null) {
      return Collections.emptySet();
    }
    Set<Long> calificados = new HashSet<>(repoArbitro.encuentrosCalificadosPor(usuario.getId()));
    Set<Long> ids = new HashSet<>();
    for (FaseTorneo fase : repoFase.listarPorTorneo(torneoId)) {
      for (EncuentroTorneo encuentro : fase.getEncuentros()) {
        if (!calificados.contains(encuentro.getId()) && habilitado(usuario, encuentro)) {
          ids.add(encuentro.getId());
        }
      }
    }
    return ids;
  }

  @Override
  public Set<Long> partidosLigaCalificables(Usuario usuario, Long ligaId) {
    if (usuario == null || usuario.getId() == null || ligaId == null) {
      return Collections.emptySet();
    }
    Set<Long> calificados = new HashSet<>(repoArbitro.partidosLigaCalificadosPor(usuario.getId()));
    Set<Long> ids = new HashSet<>();
    for (PartidoLiga partido : repoPartidoLiga.listarPorLiga(ligaId)) {
      if (!calificados.contains(partido.getId()) && puedeCalificarLiga(usuario, partido)) {
        ids.add(partido.getId());
      }
    }
    return ids;
  }

  private boolean aplicar(
    Usuario usuario,
    Arbitro arbitro,
    Integer puntaje,
    Consumer<CalificacionArbitro> vincular
  ) {
    arbitro.calificar(puntaje);
    repoArbitro.actualizar(arbitro);
    CalificacionArbitro calificacion = new CalificacionArbitro();
    calificacion.setArbitro(arbitro);
    calificacion.setUsuario(usuario);
    calificacion.setPuntaje(puntaje);
    vincular.accept(calificacion);
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
    if (encuentro == null || !habilitado(usuario, encuentro)) {
      return false;
    }
    return !repoArbitro.existeCalificacion(usuario.getId(), encuentro.getId());
  }

  private static boolean habilitado(Usuario usuario, EncuentroTorneo encuentro) {
    if (encuentro.getArbitro() == null || !ESTADO_JUGADO.equals(encuentro.getEstado())) {
      return false;
    }
    return esAdmin(usuario) || participo(usuario, encuentro);
  }

  private static boolean puedeCalificarLiga(Usuario usuario, PartidoLiga partido) {
    if (partido == null || partido.getArbitro() == null || !partido.estaJugado()) {
      return false;
    }
    return (
      esAdmin(usuario) ||
      perteneceALiga(usuario, partido.getLocal()) ||
      perteneceALiga(usuario, partido.getVisitante())
    );
  }

  private static boolean esAdmin(Usuario usuario) {
    return ROL_ADMIN.equalsIgnoreCase(usuario.getRol());
  }

  private static boolean participo(Usuario usuario, EncuentroTorneo encuentro) {
    return (
      perteneceATorneo(usuario, encuentro.getParticipanteA()) ||
      perteneceATorneo(usuario, encuentro.getParticipanteB())
    );
  }

  private static boolean perteneceATorneo(Usuario usuario, InscripcionTorneo inscripcion) {
    return (
      inscripcion != null && esMiembro(usuario, inscripcion.getUsuario(), inscripcion.getEquipo())
    );
  }

  private static boolean perteneceALiga(Usuario usuario, InscripcionLiga inscripcion) {
    return (
      inscripcion != null && esMiembro(usuario, inscripcion.getUsuario(), inscripcion.getEquipo())
    );
  }

  private static boolean esMiembro(Usuario usuario, Usuario inscriptor, Equipo equipo) {
    Long id = usuario.getId();
    if (inscriptor != null && id.equals(inscriptor.getId())) {
      return true;
    }
    if (equipo == null || equipo.getJugadores() == null) {
      return false;
    }
    return equipo.getJugadores().stream().anyMatch(jugador -> id.equals(jugador.getId()));
  }
}
