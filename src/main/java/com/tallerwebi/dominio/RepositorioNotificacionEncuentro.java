package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioNotificacionEncuentro {
  void guardar(NotificacionEncuentro notificacion);
  List<NotificacionEncuentro> listarNoLeidas(Long usuarioId);
}
