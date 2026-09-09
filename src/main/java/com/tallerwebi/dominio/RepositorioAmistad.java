package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioAmistad {
  void guardar(Amistad amistad);
  boolean existeRelacion(Long usuarioId, Long amigoId);
  List<Usuario> listarAmigosDe(Long usuarioId);
}
