package com.tallerwebi.dominio;

import java.util.List;

@FunctionalInterface
public interface ServicioEstadisticasEquipo {
  List<EstadisticaEquipo> estadisticasDelUsuario(Usuario usuario);
}
