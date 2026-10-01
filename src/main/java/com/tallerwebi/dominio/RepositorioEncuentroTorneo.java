package com.tallerwebi.dominio;

public interface RepositorioEncuentroTorneo {
  EncuentroTorneo buscarPorId(Long id);
  void guardar(EncuentroTorneo encuentro);
  void actualizar(EncuentroTorneo encuentro);
}
