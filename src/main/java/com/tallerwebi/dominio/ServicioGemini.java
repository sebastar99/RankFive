package com.tallerwebi.dominio;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ServicioGemini {
  String generarTexto(String prompt) throws JsonProcessingException;

  ImagenGenerada generarImagen(String prompt) throws JsonProcessingException;
}
