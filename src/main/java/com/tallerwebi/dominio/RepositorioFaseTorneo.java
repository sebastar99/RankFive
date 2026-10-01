package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioFaseTorneo {
  void guardar(FaseTorneo fase);
  List<FaseTorneo> listarPorTorneo(Long torneoId);
}
