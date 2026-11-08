package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioEncuentroTorneo {
  EncuentroTorneo buscarPorId(Long id);
  void guardar(EncuentroTorneo encuentro);
  void actualizar(EncuentroTorneo encuentro);
  List<EncuentroTorneo> listarJugados();
}
