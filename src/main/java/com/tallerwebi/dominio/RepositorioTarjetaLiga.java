package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.TipoTarjeta;
import java.util.List;

public interface RepositorioTarjetaLiga {
  void guardar(TarjetaLiga tarjeta);

  void borrarPorPartido(Long partidoId);

  List<TarjetaLiga> listarPorLiga(Long ligaId);

  long contarDe(Long usuarioId, TipoTarjeta tipo);
}
