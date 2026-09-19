package com.tallerwebi.dominio;

@FunctionalInterface
public interface AlmacenamientoImagenes {
  /**
   * Persiste la imagen y devuelve la URL publica con la que puede servirse.
   * Devuelve null si el contenido esta vacio.
   */
  String guardarEscudo(String nombreOriginal, byte[] contenido);
}
