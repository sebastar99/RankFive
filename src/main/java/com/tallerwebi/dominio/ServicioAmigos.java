package com.tallerwebi.dominio;

@FunctionalInterface
public interface ServicioAmigos {
  Usuario buscarAmigoPorNombreDeUsuario(String nombreUsuario);
}
