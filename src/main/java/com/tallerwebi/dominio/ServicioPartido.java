package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioPartido {
  Partido crearPartido(List<Usuario> equipoA, List<Usuario> equipoB);

  Partido crearPartido(List<Usuario> equipoA, List<Usuario> equipoB, java.time.LocalDateTime fecha);

  boolean registrarResultado(Long partidoId, int golesEquipoA, int golesEquipoB);

  List<Partido> listarPartidosDe(Usuario usuario);

  Usuario buscarPorEmail(String email);

  List<NotificacionPl> listarNotificacionesNoLeidas(Usuario usuario);

  List<NotificacionPartido> listarNotificacionesPartidoNoLeidas(Usuario usuario);

  void marcarNotificacionesLeidas(Usuario usuario);

  List<NotificacionPl> listarNotificacionesDe(Usuario usuario);

  boolean registrarDetalles(
    Long partidoId,
    List<Long> golesAIds,
    List<Long> asistenciasAIds,
    List<Long> golesBIds,
    List<Long> asistenciasBIds
  );
}
