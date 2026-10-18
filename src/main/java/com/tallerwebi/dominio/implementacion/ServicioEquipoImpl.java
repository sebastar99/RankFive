package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadoEquipo;
import com.tallerwebi.dominio.EstadoInvitacionEquipo;
import com.tallerwebi.dominio.InvitacionEquipo;
import com.tallerwebi.dominio.RepositorioEquipo;
import com.tallerwebi.dominio.RepositorioInvitacionEquipo;
import com.tallerwebi.dominio.ResultadoCreacionEquipo;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.ResultadoAceptacionInvitacion;
import com.tallerwebi.dominio.enums.ResultadoTransferencia;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioEquipo")
@Transactional
public class ServicioEquipoImpl implements ServicioEquipo {

  private static final Set<Integer> FORMATOS_VALIDOS = Set.of(5, 7, 8, 9, 11);

  private final RepositorioEquipo repositorioEquipo;
  private final RepositorioInvitacionEquipo repositorioInvitacion;
  private final ServicioRelacionAmistad servicioRelacionAmistad;

  @Autowired
  public ServicioEquipoImpl(
    RepositorioEquipo repositorioEquipo,
    RepositorioInvitacionEquipo repositorioInvitacion,
    ServicioRelacionAmistad servicioRelacionAmistad
  ) {
    this.repositorioEquipo = repositorioEquipo;
    this.repositorioInvitacion = repositorioInvitacion;
    this.servicioRelacionAmistad = servicioRelacionAmistad;
  }

  @Override
  public ResultadoCreacionEquipo crearEquipo(
    Usuario owner,
    String nombre,
    Integer formato,
    String fotoUrl,
    List<String> nombresUsuarioInvitados
  ) {
    Set<String> invitados = normalizar(nombresUsuarioInvitados, owner);
    ResultadoCreacionEquipo validacion = validar(owner, nombre, formato, invitados);
    if (validacion != null) {
      return validacion;
    }
    List<Usuario> jugadoresInvitados = resolverAmigos(owner, invitados);
    if (jugadoresInvitados.size() != invitados.size()) {
      return ResultadoCreacionEquipo.JUGADOR_NO_AMIGO;
    }

    Equipo equipo = new Equipo();
    equipo.setNombre(nombre.trim());
    equipo.setOwner(owner);
    equipo.setFormato(formato);
    equipo.setFotoUrl(fotoUrl);
    equipo.setEstado(EstadoEquipo.PENDIENTE);
    equipo.agregarJugador(owner);
    repositorioEquipo.guardar(equipo);

    for (Usuario jugador : jugadoresInvitados) {
      InvitacionEquipo invitacion = new InvitacionEquipo();
      invitacion.setEquipo(equipo);
      invitacion.setUsuario(jugador);
      invitacion.setEstado(EstadoInvitacionEquipo.PENDIENTE);
      repositorioInvitacion.guardar(invitacion);
    }
    return ResultadoCreacionEquipo.CREADO;
  }

  @Override
  public boolean aceptarInvitacion(Long invitacionId, Usuario usuarioActual) {
    return (
      aceptarInvitacionConResultado(invitacionId, usuarioActual) ==
      ResultadoAceptacionInvitacion.ACEPTADA
    );
  }

  @Override
  public ResultadoAceptacionInvitacion aceptarInvitacionConResultado(
    Long invitacionId,
    Usuario usuarioActual
  ) {
    InvitacionEquipo invitacion = invitacionRespondible(invitacionId, usuarioActual);
    if (invitacion == null) {
      return ResultadoAceptacionInvitacion.INVITACION_INVALIDA;
    }
    Equipo origen = invitacion.getEquipoOrigen();
    ResultadoAceptacionInvitacion error = validarSalida(origen, usuarioActual.getId());
    if (error != null) {
      return error;
    }
    invitacion.setEstado(EstadoInvitacionEquipo.ACEPTADA);
    invitacion.setRespuestaPostergada(false);
    invitacion.setRespuestaLeida(false);
    repositorioInvitacion.actualizar(invitacion);

    Equipo equipo = invitacion.getEquipo();
    equipo.agregarJugador(invitacion.getUsuario());
    if (todasAceptadas(equipo)) {
      equipo.setEstado(EstadoEquipo.ACTIVO);
    }
    if (origen != null) {
      origen.quitarJugador(invitacion.getUsuario());
      repositorioEquipo.actualizar(origen);
    }
    repositorioEquipo.actualizar(equipo);
    return ResultadoAceptacionInvitacion.ACEPTADA;
  }

  private ResultadoAceptacionInvitacion validarSalida(Equipo origen, Long jugadorId) {
    if (origen == null) {
      return null;
    }
    if (jugadorDe(origen, jugadorId) == null) {
      return ResultadoAceptacionInvitacion.INVITACION_INVALIDA;
    }
    int minimoCompetencias = repositorioEquipo.minimoJugadoresEnCompetencias(origen.getId());
    int minimo = minimoCompetencias > 0 ? minimoCompetencias : origen.getFormato();
    if (origen.getJugadores().size() <= minimo) {
      return ResultadoAceptacionInvitacion.JUGADORES_INSUFICIENTES;
    }
    return null;
  }

  @Override
  public boolean rechazarInvitacion(Long invitacionId, Usuario usuarioActual) {
    InvitacionEquipo invitacion = invitacionRespondible(invitacionId, usuarioActual);
    if (invitacion == null) {
      return false;
    }
    invitacion.setEstado(EstadoInvitacionEquipo.RECHAZADA);
    invitacion.setRespuestaPostergada(false);
    invitacion.setRespuestaLeida(false);
    repositorioInvitacion.actualizar(invitacion);
    return true;
  }

  @Override
  public boolean postergarOferta(Long invitacionId, Usuario usuarioActual) {
    InvitacionEquipo invitacion = invitacionRespondible(invitacionId, usuarioActual);
    if (invitacion == null || !invitacion.puedePostergarse()) {
      return false;
    }
    invitacion.setRespuestaPostergada(true);
    repositorioInvitacion.actualizar(invitacion);
    return true;
  }

  @Override
  public boolean marcarRespuestaLeida(Long invitacionId, Usuario usuarioActual) {
    if (invitacionId == null || sinId(usuarioActual)) {
      return false;
    }
    InvitacionEquipo invitacion = repositorioInvitacion.buscarPorId(invitacionId);
    if (invitacion == null || !invitacion.esRespuestaVisiblePara(usuarioActual.getId())) {
      return false;
    }
    invitacion.setRespuestaLeida(true);
    repositorioInvitacion.actualizar(invitacion);
    return true;
  }

  @Override
  public List<InvitacionEquipo> listarRespuestasTransferencia(Usuario usuarioActual) {
    if (sinId(usuarioActual)) {
      return Collections.emptyList();
    }
    return repositorioInvitacion.listarRespuestasParaCapitan(usuarioActual.getId());
  }

  @Override
  public List<Equipo> listarEquiposActivosDe(Usuario usuarioActual) {
    if (sinId(usuarioActual)) {
      return Collections.emptyList();
    }
    return repositorioEquipo.listarActivosDe(usuarioActual.getId());
  }

  @Override
  public List<InvitacionEquipo> listarInvitacionesPendientes(Usuario usuarioActual) {
    if (sinId(usuarioActual)) {
      return Collections.emptyList();
    }
    return repositorioInvitacion.listarPendientesPara(usuarioActual.getId());
  }

  @Override
  public InvitacionEquipo buscarOfertaPendiente(Long invitacionId, Usuario usuarioActual) {
    InvitacionEquipo invitacion = invitacionRespondible(invitacionId, usuarioActual);
    if (invitacion == null || !invitacion.esPase()) {
      return null;
    }
    return invitacion;
  }

  @Override
  public long contarInvitacionesPendientes(Usuario usuarioActual) {
    if (sinId(usuarioActual)) {
      return 0L;
    }
    return repositorioInvitacion.contarPendientesPara(usuarioActual.getId());
  }

  @Override
  public ResultadoTransferencia ofrecerTransferencia(
    Usuario capitan,
    Long destinoId,
    Long origenId,
    Long jugadorId
  ) {
    if (sinId(capitan)) {
      return ResultadoTransferencia.USUARIO_INVALIDO;
    }
    Equipo destino = destinoId == null ? null : repositorioEquipo.buscarPorId(destinoId);
    Equipo origen = origenId == null ? null : repositorioEquipo.buscarPorId(origenId);

    ResultadoTransferencia error = validarOferta(capitan, destino, origen, jugadorId);
    if (error != null) {
      return error;
    }

    InvitacionEquipo invitacion = new InvitacionEquipo();
    invitacion.setEquipo(destino);
    invitacion.setEquipoOrigen(origen);
    invitacion.setUsuario(jugadorDe(origen, jugadorId));
    invitacion.setEstado(EstadoInvitacionEquipo.PENDIENTE);
    repositorioInvitacion.guardar(invitacion);
    return ResultadoTransferencia.OFERTA_ENVIADA;
  }

  private ResultadoTransferencia validarOferta(
    Usuario capitan,
    Equipo destino,
    Equipo origen,
    Long jugadorId
  ) {
    if (destino == null || origen == null) {
      return ResultadoTransferencia.EQUIPO_INVALIDO;
    }
    if (destino.getOwner() == null || !capitan.getId().equals(destino.getOwner().getId())) {
      return ResultadoTransferencia.NO_SOS_CAPITAN;
    }
    if (jugadorDe(origen, jugadorId) == null) {
      return ResultadoTransferencia.JUGADOR_NO_PERTENECE;
    }
    if (jugadorDe(destino, jugadorId) != null) {
      return ResultadoTransferencia.YA_ES_INTEGRANTE;
    }
    if (hayOfertaPendiente(destino.getId(), jugadorId)) {
      return ResultadoTransferencia.OFERTA_YA_PENDIENTE;
    }
    return null;
  }

  private boolean hayOfertaPendiente(Long destinoId, Long jugadorId) {
    return repositorioInvitacion
      .listarDeEquipo(destinoId)
      .stream()
      .anyMatch(i -> i.estaPendiente() && i.fueEnviadaA(jugadorId));
  }

  private static boolean sinId(Usuario usuario) {
    return usuario == null || usuario.getId() == null;
  }

  private static Usuario jugadorDe(Equipo equipo, Long jugadorId) {
    if (jugadorId == null) {
      return null;
    }
    return equipo
      .getJugadores()
      .stream()
      .filter(j -> jugadorId.equals(j.getId()))
      .findFirst()
      .orElse(null);
  }

  private static ResultadoCreacionEquipo validar(
    Usuario owner,
    String nombre,
    Integer formato,
    Set<String> invitados
  ) {
    if (sinId(owner)) {
      return ResultadoCreacionEquipo.USUARIO_INVALIDO;
    }
    if (nombre == null || nombre.isBlank()) {
      return ResultadoCreacionEquipo.NOMBRE_INVALIDO;
    }
    if (formato == null || !FORMATOS_VALIDOS.contains(formato)) {
      return ResultadoCreacionEquipo.FORMATO_INVALIDO;
    }
    if (invitados.isEmpty()) {
      return ResultadoCreacionEquipo.SIN_JUGADORES;
    }
    if (invitados.size() < formato - 1) {
      return ResultadoCreacionEquipo.JUGADORES_INSUFICIENTES;
    }
    return null;
  }

  private List<Usuario> resolverAmigos(Usuario owner, Set<String> invitados) {
    Map<String, Usuario> amigosPorUsuario = servicioRelacionAmistad
      .listarAmigosDe(owner)
      .stream()
      .filter(a -> a.getPerfil() != null && a.getPerfil().getNombreUsuario() != null)
      .collect(
        Collectors.toMap(a -> a.getPerfil().getNombreUsuario(), Function.identity(), (a, b) -> a)
      );

    List<Usuario> jugadores = invitados.stream().map(amigosPorUsuario::get).toList();
    return jugadores.contains(null) ? Collections.emptyList() : jugadores;
  }

  private static Set<String> normalizar(List<String> nombres, Usuario owner) {
    Set<String> resultado = new LinkedHashSet<>();
    if (nombres == null || owner == null) {
      return resultado;
    }
    String propio = owner.getPerfil() != null ? owner.getPerfil().getNombreUsuario() : null;
    nombres
      .stream()
      .filter(Objects::nonNull)
      .map(String::trim)
      .filter(n -> !n.isBlank() && !n.equals(propio))
      .forEach(resultado::add);
    return resultado;
  }

  private InvitacionEquipo invitacionRespondible(Long invitacionId, Usuario usuarioActual) {
    if (invitacionId == null || sinId(usuarioActual)) {
      return null;
    }
    InvitacionEquipo invitacion = repositorioInvitacion.buscarPorId(invitacionId);
    if (invitacion == null || !invitacion.esRespondiblePor(usuarioActual.getId())) {
      return null;
    }
    return invitacion;
  }

  private boolean todasAceptadas(Equipo equipo) {
    return repositorioInvitacion
      .listarDeEquipo(equipo.getId())
      .stream()
      .allMatch(InvitacionEquipo::fueAceptada);
  }
}
