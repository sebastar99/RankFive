package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.ResultadoAceptacionInvitacion;
import com.tallerwebi.dominio.enums.ResultadoTransferencia;
import java.util.List;

public interface ServicioEquipo {
  ResultadoCreacionEquipo crearEquipo(
    Usuario owner,
    String nombre,
    Integer formato,
    String fotoUrl,
    List<String> nombresUsuarioInvitados
  );

  boolean aceptarInvitacion(Long invitacionId, Usuario usuarioActual);

  ResultadoAceptacionInvitacion aceptarInvitacionConResultado(
    Long invitacionId,
    Usuario usuarioActual
  );

  boolean rechazarInvitacion(Long invitacionId, Usuario usuarioActual);

  boolean postergarOferta(Long invitacionId, Usuario usuarioActual);

  boolean marcarRespuestaLeida(Long invitacionId, Usuario usuarioActual);

  List<InvitacionEquipo> listarRespuestasTransferencia(Usuario usuarioActual);

  List<Equipo> listarEquiposActivosDe(Usuario usuarioActual);

  List<InvitacionEquipo> listarInvitacionesPendientes(Usuario usuarioActual);

  InvitacionEquipo buscarOfertaPendiente(Long invitacionId, Usuario usuarioActual);

  long contarInvitacionesPendientes(Usuario usuarioActual);

  ResultadoTransferencia ofrecerTransferencia(
    Usuario capitan,
    Long destinoId,
    Long origenId,
    Long jugadorId
  );
}
