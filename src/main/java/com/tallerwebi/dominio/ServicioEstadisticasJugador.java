package com.tallerwebi.dominio;

@FunctionalInterface
public interface ServicioEstadisticasJugador {
  EstadisticasJugador obtenerEstadisticasDelUsuario(Long usuarioId);
}
