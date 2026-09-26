package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioLiga {
  void guardar(Liga liga);
  Liga buscarPorId(Long id);
  List<Liga> listarTodos();
}
