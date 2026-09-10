package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioRelacionAmistad {
  ResultadoSolicitud enviarSolicitud(Usuario solicitante, Usuario destinatario);

  boolean aceptarSolicitud(Long solicitudId, Usuario usuarioActual);

  boolean rechazarSolicitud(Long solicitudId, Usuario usuarioActual);

  List<Usuario> listarAmigosDe(Usuario usuarioActual);

  List<Amistad> listarSolicitudesPendientes(Usuario usuarioActual);

  long contarSolicitudesPendientes(Usuario usuarioActual);
}
