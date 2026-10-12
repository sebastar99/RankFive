package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioTarjetaPartido {
  public void guardar(TarjetaPartido tarjeta);

  public void borrarPorPartido(Long partidoId);

  public List<TarjetaPartido> listarTarjetasPorPartido(Long partidoId);
}
