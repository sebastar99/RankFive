package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioNotificacionPl {
  void guardar(NotificacionPl notificacion);

  List<NotificacionPl> listarNoLeidasDe(Long usuarioId);

  void marcarLeidasDe(Long usuarioId);

  List<NotificacionPl> listarDe(Long usuarioId);
}
