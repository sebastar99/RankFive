package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.TipoTarjeta;
import java.util.List;

public interface RepositorioTarjetaTorneo {
  void guardar(TarjetaTorneo tarjeta);

  void borrarPorEncuentro(Long encuentroId);

  List<TarjetaTorneo> listarPorTorneo(Long torneoId);

  long contarDe(Long usuarioId, TipoTarjeta tipo);
}
