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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    InvitacionEquipo invitacion = invitacionRespondible(invitacionId, usuarioActual);
    if (invitacion == null) {
      return false;
    }
    invitacion.setEstado(EstadoInvitacionEquipo.ACEPTADA);
    repositorioInvitacion.actualizar(invitacion);

    Equipo equipo = invitacion.getEquipo();
    equipo.agregarJugador(invitacion.getUsuario());
    if (todasAceptadas(equipo)) {
      equipo.setEstado(EstadoEquipo.ACTIVO);
    }
    repositorioEquipo.actualizar(equipo);
    return true;
  }

  @Override
  public boolean rechazarInvitacion(Long invitacionId, Usuario usuarioActual) {
    InvitacionEquipo invitacion = invitacionRespondible(invitacionId, usuarioActual);
    if (invitacion == null) {
      return false;
    }
    invitacion.setEstado(EstadoInvitacionEquipo.RECHAZADA);
    repositorioInvitacion.actualizar(invitacion);
    return true;
  }

  @Override
  public List<Equipo> listarEquiposActivosDe(Usuario usuarioActual) {
    if (usuarioActual == null || usuarioActual.getId() == null) {
      return Collections.emptyList();
    }
    return repositorioEquipo.listarActivosDe(usuarioActual.getId());
  }

  @Override
  public List<InvitacionEquipo> listarInvitacionesPendientes(Usuario usuarioActual) {
    if (usuarioActual == null || usuarioActual.getId() == null) {
      return Collections.emptyList();
    }
    return repositorioInvitacion.listarPendientesPara(usuarioActual.getId());
  }

  @Override
  public long contarInvitacionesPendientes(Usuario usuarioActual) {
    if (usuarioActual == null || usuarioActual.getId() == null) {
      return 0L;
    }
    return repositorioInvitacion.contarPendientesPara(usuarioActual.getId());
  }

  private static ResultadoCreacionEquipo validar(
    Usuario owner,
    String nombre,
    Integer formato,
    Set<String> invitados
  ) {
    if (owner == null || owner.getId() == null) {
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
    if (invitados.size() > formato - 1) {
      return ResultadoCreacionEquipo.DEMASIADOS_JUGADORES;
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

    List<Usuario> jugadores = new ArrayList<>();
    for (String nombreUsuario : invitados) {
      Usuario amigo = amigosPorUsuario.get(nombreUsuario);
      if (amigo == null) {
        return Collections.emptyList();
      }
      jugadores.add(amigo);
    }
    return jugadores;
  }

  private static Set<String> normalizar(List<String> nombres, Usuario owner) {
    Set<String> resultado = new LinkedHashSet<>();
    if (nombres == null || owner == null) {
      return resultado;
    }
    String propio = owner.getPerfil() != null ? owner.getPerfil().getNombreUsuario() : null;
    for (String nombre : nombres) {
      if (nombre == null || nombre.isBlank() || nombre.equals(propio)) {
        continue;
      }
      resultado.add(nombre.trim());
    }
    return resultado;
  }

  private InvitacionEquipo invitacionRespondible(Long invitacionId, Usuario usuarioActual) {
    if (invitacionId == null || usuarioActual == null || usuarioActual.getId() == null) {
      return null;
    }
    InvitacionEquipo invitacion = repositorioInvitacion.buscarPorId(invitacionId);
    if (invitacion == null || !invitacion.estaPendiente()) {
      return null;
    }
    if (!invitacion.fueEnviadaA(usuarioActual.getId())) {
      return null;
    }
    return invitacion;
  }

  private boolean todasAceptadas(Equipo equipo) {
    List<InvitacionEquipo> invitaciones = repositorioInvitacion.listarDeEquipo(equipo.getId());
    for (InvitacionEquipo invitacion : invitaciones) {
      if (!invitacion.fueAceptada()) {
        return false;
      }
    }
    return true;
  }
}
