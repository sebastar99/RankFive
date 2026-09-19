package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioInvitacionEquipo {
  void guardar(InvitacionEquipo invitacion);

  void actualizar(InvitacionEquipo invitacion);

  InvitacionEquipo buscarPorId(Long id);

  List<InvitacionEquipo> listarDeEquipo(Long equipoId);

  List<InvitacionEquipo> listarPendientesPara(Long usuarioId);

  long contarPendientesPara(Long usuarioId);
}
