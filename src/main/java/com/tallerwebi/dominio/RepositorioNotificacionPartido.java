package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioNotificacionPartido {
  void guardar(NotificacionPartido notificacion);

  List<NotificacionPartido> listarNoLeidasDe(Long usuarioId);

  void marcarLeidasDe(Long usuarioId);
}
