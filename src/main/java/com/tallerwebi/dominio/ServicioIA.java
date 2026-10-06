package com.tallerwebi.dominio;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ServicioIA {
  String generarPreviaPartido(Long partidoId) throws JsonProcessingException;

  ImagenGenerada generarBannerPartido(Long partidoId) throws JsonProcessingException;
}
