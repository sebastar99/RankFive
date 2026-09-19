package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Amistad;
import com.tallerwebi.dominio.EstadoAmistad;
import com.tallerwebi.dominio.RepositorioAmistad;
import com.tallerwebi.dominio.ResultadoSolicitud;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioRelacionAmistad")
@Transactional
public class ServicioRelacionAmistadImpl implements ServicioRelacionAmistad {

  private final RepositorioAmistad repositorioAmistad;

  @Autowired
  public ServicioRelacionAmistadImpl(RepositorioAmistad repositorioAmistad) {
    this.repositorioAmistad = repositorioAmistad;
  }

  @Override
  public ResultadoSolicitud enviarSolicitud(Usuario solicitante, Usuario destinatario) {
    if (!sonUsuariosValidos(solicitante, destinatario)) {
      return ResultadoSolicitud.USUARIO_INVALIDO;
    }
    if (solicitante.getId().equals(destinatario.getId())) {
      return ResultadoSolicitud.ES_UNO_MISMO;
    }

    Amistad existente = repositorioAmistad.buscarRelacionEntre(
      solicitante.getId(),
      destinatario.getId()
    );
    if (existente != null) {
      return resultadoSegunRelacionExistente(existente, solicitante);
    }

    Amistad solicitud = new Amistad();
    solicitud.setUsuario(solicitante);
    solicitud.setAmigo(destinatario);
    solicitud.setEstado(EstadoAmistad.PENDIENTE);
    repositorioAmistad.guardar(solicitud);
    return ResultadoSolicitud.ENVIADA;
  }

  @Override
  public boolean aceptarSolicitud(Long solicitudId, Usuario usuarioActual) {
    return responderSolicitud(solicitudId, usuarioActual, EstadoAmistad.ACEPTADA);
  }

  @Override
  public boolean rechazarSolicitud(Long solicitudId, Usuario usuarioActual) {
    return responderSolicitud(solicitudId, usuarioActual, EstadoAmistad.RECHAZADA);
  }

  @Override
  public List<Usuario> listarAmigosDe(Usuario usuarioActual) {
    if (usuarioActual == null || usuarioActual.getId() == null) {
      return Collections.emptyList();
    }
    return repositorioAmistad.listarAmigosDe(usuarioActual.getId());
  }

  @Override
  public List<Amistad> listarSolicitudesPendientes(Usuario usuarioActual) {
    if (usuarioActual == null || usuarioActual.getId() == null) {
      return Collections.emptyList();
    }
    return repositorioAmistad.listarSolicitudesPendientesPara(usuarioActual.getId());
  }

  @Override
  public long contarSolicitudesPendientes(Usuario usuarioActual) {
    if (usuarioActual == null || usuarioActual.getId() == null) {
      return 0L;
    }
    return repositorioAmistad.contarSolicitudesPendientesPara(usuarioActual.getId());
  }

  private boolean sonUsuariosValidos(Usuario solicitante, Usuario destinatario) {
    return (
      solicitante != null &&
      destinatario != null &&
      solicitante.getId() != null &&
      destinatario.getId() != null
    );
  }

  private ResultadoSolicitud resultadoSegunRelacionExistente(
    Amistad existente,
    Usuario solicitante
  ) {
    if (EstadoAmistad.ACEPTADA.equals(existente.getEstado())) {
      return ResultadoSolicitud.YA_SON_AMIGOS;
    }
    if (solicitante.getId().equals(existente.getUsuario().getId())) {
      return ResultadoSolicitud.SOLICITUD_YA_ENVIADA;
    }
    return ResultadoSolicitud.SOLICITUD_RECIBIDA_PENDIENTE;
  }

  private boolean responderSolicitud(
    Long solicitudId,
    Usuario usuarioActual,
    EstadoAmistad nuevoEstado
  ) {
    if (solicitudId == null || usuarioActual == null || usuarioActual.getId() == null) {
      return false;
    }
    Amistad solicitud = repositorioAmistad.buscarPorId(solicitudId);
    if (solicitud == null || !solicitud.estaPendiente()) {
      return false;
    }
    if (!solicitud.fueEnviadaA(usuarioActual.getId())) {
      return false;
    }
    solicitud.setEstado(nuevoEstado);
    repositorioAmistad.actualizar(solicitud);
    return true;
  }
}
