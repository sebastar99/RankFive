package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioRelacionAmistad {
  void agregarAmigo(Usuario usuarioActual, Usuario usuarioAmigo);
  List<Usuario> listarAmigosDe(Usuario usuarioActual);
}
