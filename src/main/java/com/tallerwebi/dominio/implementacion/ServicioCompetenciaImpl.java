package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.FilaPosicion;
import com.tallerwebi.dominio.GeneradorFixtureTorneo;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.NotificacionEncuentro;
import com.tallerwebi.dominio.RepositorioEncuentroTorneo;
import com.tallerwebi.dominio.RepositorioFaseTorneo;
import com.tallerwebi.dominio.RepositorioInscripcionTorneo;
import com.tallerwebi.dominio.RepositorioLiga;
import com.tallerwebi.dominio.RepositorioNotificacionEncuentro;
import com.tallerwebi.dominio.RepositorioTorneo;
import com.tallerwebi.dominio.ResultadoInscripcion;
import com.tallerwebi.dominio.ServicioArbitro;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.TablaPosiciones;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioCompetencia")
@Transactional
public class ServicioCompetenciaImpl implements ServicioCompetencia {

  private static final int MINIMO_EQUIPOS = 2;

  private final RepositorioTorneo repoTorneo;
  private final RepositorioLiga repoLiga;
  private final RepositorioInscripcionTorneo repoInscripcion;
  private final RepositorioFaseTorneo repoFase;
  private final RepositorioEncuentroTorneo repoEncuentro;
  private final RepositorioNotificacionEncuentro repoNotifEncuentro;
  private final ServicioEquipo servicioEquipo;
  private final ServicioArbitro servicioArbitro;

  @Autowired
  public ServicioCompetenciaImpl(
    RepositorioTorneo repoTorneo,
    RepositorioLiga repoLiga,
    RepositorioInscripcionTorneo repoInscripcion,
    RepositorioFaseTorneo repoFase,
    RepositorioEncuentroTorneo repoEncuentro,
    RepositorioNotificacionEncuentro repoNotifEncuentro,
    ServicioEquipo servicioEquipo,
    ServicioArbitro servicioArbitro
  ) {
    this.repoTorneo = repoTorneo;
    this.repoLiga = repoLiga;
    this.repoInscripcion = repoInscripcion;
    this.repoFase = repoFase;
    this.repoEncuentro = repoEncuentro;
    this.repoNotifEncuentro = repoNotifEncuentro;
    this.servicioEquipo = servicioEquipo;
    this.servicioArbitro = servicioArbitro;
  }

  @Override
  public List<Torneo> listarTorneos() {
    List<Torneo> lista = repoTorneo.listarTodos();
    return lista == null ? Collections.emptyList() : lista;
  }

  @Override
  public List<Liga> listarLigas() {
    List<Liga> lista = repoLiga.listarTodos();
    return lista == null ? Collections.emptyList() : lista;
  }

  @Override
  public ResultadoInscripcion inscribirATorneo(Usuario usuario, Long torneoId) {
    if (usuario == null || usuario.getId() == null || torneoId == null) {
      return ResultadoInscripcion.INVALIDO;
    }
    Torneo torneo = repoTorneo.buscarPorId(torneoId);
    ResultadoInscripcion validacion = validarInscripcion(usuario, torneo);
    if (validacion != null) {
      return validacion;
    }
    List<Equipo> equipos = servicioEquipo.listarEquiposActivosDe(usuario);
    if (equipos == null || equipos.isEmpty()) {
      return ResultadoInscripcion.SIN_EQUIPO_ACTIVO;
    }
    Equipo equipoSeleccionado = equipos.get(0);
    InscripcionTorneo insc = new InscripcionTorneo();
    insc.setUsuario(usuario);
    insc.setTorneo(torneo);
    insc.setEquipo(equipoSeleccionado);
    insc.setNombreEquipo(equipoSeleccionado.getNombre());
    insc.setGrupo(
      GeneradorFixtureTorneo.grupoDisponible(repoInscripcion.listarPorTorneo(torneoId))
    );
    repoInscripcion.guardar(insc);
    return ResultadoInscripcion.INSCRIPTO;
  }

  private ResultadoInscripcion validarInscripcion(Usuario usuario, Torneo torneo) {
    if (torneo == null) {
      return ResultadoInscripcion.INVALIDO;
    }
    if (
      !Boolean.TRUE.equals(torneo.getInscripcionAbierta()) ||
      !repoFase.listarPorTorneo(torneo.getId()).isEmpty()
    ) {
      return ResultadoInscripcion.INSCRIPCION_CERRADA;
    }
    if (repoInscripcion.existePara(usuario.getId(), torneo.getId())) {
      return ResultadoInscripcion.YA_INSCRIPTO;
    }
    Integer cupo = torneo.getCupoEquipos();
    if (cupo != null && repoInscripcion.listarPorTorneo(torneo.getId()).size() >= cupo) {
      return ResultadoInscripcion.CUPO_COMPLETO;
    }
    return null;
  }

  @Override
  public boolean tieneEquipoActivo(Usuario usuario) {
    List<Equipo> equipos = servicioEquipo.listarEquiposActivosDe(usuario);
    return equipos != null && !equipos.isEmpty();
  }

  @Override
  public Map<String, List<FilaPosicion>> calcularTablasPorGrupo(Long torneoId) {
    if (torneoId == null) {
      return Collections.emptyMap();
    }
    return TablaPosiciones.calcularPorGrupo(
      repoInscripcion.listarPorTorneo(torneoId),
      repoFase.listarPorTorneo(torneoId)
    );
  }

  @Override
  public InscripcionTorneo obtenerCampeon(Long torneoId) {
    if (torneoId == null) {
      return null;
    }
    return GeneradorFixtureTorneo.campeon(repoFase.listarPorTorneo(torneoId));
  }

  @Override
  public List<Equipo> listarEquiposInscriptos(Long torneoId) {
    if (torneoId == null) {
      return Collections.emptyList();
    }
    return repoInscripcion.listarEquipos(torneoId);
  }

  @Override
  public boolean yaInscripto(Usuario usuario, Long torneoId) {
    if (usuario == null || usuario.getId() == null || torneoId == null) {
      return false;
    }
    return repoInscripcion.existePara(usuario.getId(), torneoId);
  }

  @Override
  public java.util.List<InscripcionTorneo> listarInscripciones(Long torneoId) {
    if (torneoId == null) {
      return java.util.Collections.emptyList();
    }
    return repoInscripcion.listarPorTorneo(torneoId);
  }

  @Override
  public Torneo buscarTorneoPorId(Long torneoId) {
    if (torneoId == null) {
      return null;
    }
    return repoTorneo.buscarPorId(torneoId);
  }

  @Override
  public List<FaseTorneo> obtenerFases(Long torneoId) {
    if (torneoId == null) {
      return Collections.emptyList();
    }
    return repoFase.listarPorTorneo(torneoId);
  }

  @Override
  public boolean generarFixtureSiNoExiste(Long torneoId) {
    if (torneoId == null || !repoFase.listarPorTorneo(torneoId).isEmpty()) {
      return false;
    }
    Torneo torneo = repoTorneo.buscarPorId(torneoId);
    List<InscripcionTorneo> inscripciones = repoInscripcion.listarPorTorneo(torneoId);
    if (torneo == null || inscripciones.size() < MINIMO_EQUIPOS) {
      return false;
    }
    guardarFase(GeneradorFixtureTorneo.crearFaseDeGrupos(torneo, new ArrayList<>(inscripciones)));
    return true;
  }

  private void guardarFase(FaseTorneo fase) {
    servicioArbitro.designar(fase.getEncuentros());
    repoFase.guardar(fase);
    for (EncuentroTorneo enc : fase.getEncuentros()) {
      notificarParticipantes(enc, "Nuevo partido de torneo programado");
    }
  }

  @Override
  public void guardarTorneo(Torneo torneo) {
    if (torneo == null) {
      return;
    }
    repoTorneo.guardar(torneo);
  }

  @Override
  public boolean registrarResultadoEncuentro(Long encuentroId, Integer golesA, Integer golesB) {
    if (encuentroId == null || !golesValidos(golesA, golesB)) {
      return false;
    }
    EncuentroTorneo enc = repoEncuentro.buscarPorId(encuentroId);
    if (enc == null || !GeneradorFixtureTorneo.PENDIENTE.equals(enc.getEstado())) {
      return false;
    }
    FaseTorneo fase = enc.getFase();
    if (!fase.esDeGrupos() && golesA.equals(golesB)) {
      return false;
    }
    enc.setGolesA(golesA);
    enc.setGolesB(golesB);
    enc.setEstado(GeneradorFixtureTorneo.JUGADO);
    asignarGanador(enc, golesA, golesB);
    repoEncuentro.actualizar(enc);
    avanzarSiCorresponde(fase.getTorneo().getId(), fase.getId());
    return true;
  }

  private static boolean golesValidos(Integer golesA, Integer golesB) {
    return golesA != null && golesB != null && golesA >= 0 && golesB >= 0;
  }

  private void avanzarSiCorresponde(Long torneoId, Long faseId) {
    List<FaseTorneo> fases = repoFase.listarPorTorneo(torneoId);
    FaseTorneo ultima = fases.isEmpty() ? null : fases.get(fases.size() - 1);
    if (ultima == null || !ultima.getId().equals(faseId)) {
      return;
    }
    FaseTorneo siguiente = ultima.esDeGrupos()
      ? siguienteDeGrupos(ultima, torneoId)
      : siguienteEliminatoria(ultima);
    if (siguiente != null) {
      guardarFase(siguiente);
    }
  }

  private FaseTorneo siguienteDeGrupos(FaseTorneo grupos, Long torneoId) {
    if (!GeneradorFixtureTorneo.estaCompleta(grupos)) {
      return null;
    }
    return GeneradorFixtureTorneo.crearPrimeraEliminatoria(
      grupos,
      repoInscripcion.listarPorTorneo(torneoId)
    );
  }

  private FaseTorneo siguienteEliminatoria(FaseTorneo actual) {
    List<InscripcionTorneo> ganadores = GeneradorFixtureTorneo.ganadoresSiCompleta(actual);
    if (ganadores.size() < MINIMO_EQUIPOS) {
      return null;
    }
    return GeneradorFixtureTorneo.crearSiguienteEliminatoria(actual, ganadores);
  }

  private void asignarGanador(EncuentroTorneo enc, int golesA, int golesB) {
    if (golesA > golesB) {
      enc.setGanador(enc.getParticipanteA());
    } else if (golesB > golesA) {
      enc.setGanador(enc.getParticipanteB());
    } else {
      enc.setGanador(null);
    }
  }

  private void notificarParticipantes(EncuentroTorneo enc, String mensaje) {
    java.util.Set<Usuario> destinatarios = new java.util.HashSet<>();
    if (enc.getParticipanteA() != null) {
      destinatarios.add(enc.getParticipanteA().getUsuario());
      if (enc.getParticipanteA().getEquipo() != null) {
        destinatarios.addAll(enc.getParticipanteA().getEquipo().getJugadores());
      }
    }
    if (enc.getParticipanteB() != null) {
      destinatarios.add(enc.getParticipanteB().getUsuario());
      if (enc.getParticipanteB().getEquipo() != null) {
        destinatarios.addAll(enc.getParticipanteB().getEquipo().getJugadores());
      }
    }
    for (Usuario u : destinatarios) {
      if (u == null || u.getId() == null) {
        continue;
      }
      NotificacionEncuentro notif = new NotificacionEncuentro();
      notif.setUsuario(u);
      notif.setEncuentro(enc);
      notif.setMensaje(mensaje);
      repoNotifEncuentro.guardar(notif);
    }
  }

  @Override
  public void guardarLiga(Liga liga) {
    if (liga == null) {
      return;
    }
    repoLiga.guardar(liga);
  }
}
