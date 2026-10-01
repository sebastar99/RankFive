package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioTorneo {
  void guardar(Torneo torneo);
  Torneo buscarPorId(Long id);
  List<Torneo> listarTodos();
}
