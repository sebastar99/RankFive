package com.tallerwebi.dominio;

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

  boolean rechazarInvitacion(Long invitacionId, Usuario usuarioActual);

  List<Equipo> listarEquiposActivosDe(Usuario usuarioActual);

  List<InvitacionEquipo> listarInvitacionesPendientes(Usuario usuarioActual);

  long contarInvitacionesPendientes(Usuario usuarioActual);
}
