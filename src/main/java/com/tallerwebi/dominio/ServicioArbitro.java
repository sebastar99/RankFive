package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioArbitro {
  List<Arbitro> listarRanking();

  void designar(List<EncuentroTorneo> encuentros);

  boolean calificar(Usuario usuario, Long encuentroId, Integer puntaje);
}
