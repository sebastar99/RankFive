package com.tallerwebi.dominio;

import java.util.Optional;

@FunctionalInterface
public interface ServicioEstadisticasPartido {
  Optional<EstadisticasPartido> obtener(Long partidoId, Usuario usuario);
}
