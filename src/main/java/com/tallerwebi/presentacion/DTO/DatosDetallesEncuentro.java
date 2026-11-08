package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.DetallesEncuentro;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatosDetallesEncuentro {

  private List<Long> golesAIds;
  private List<Long> asistenciasAIds;
  private List<String> tiposTarjetasA;
  private List<Long> tarjetasAIds;
  private List<Long> golesBIds;
  private List<Long> asistenciasBIds;
  private List<String> tiposTarjetasB;
  private List<Long> tarjetasBIds;

  public DetallesEncuentro aDominio() {
    return new DetallesEncuentro(
      golesAIds,
      asistenciasAIds,
      tiposTarjetasA,
      tarjetasAIds,
      golesBIds,
      asistenciasBIds,
      tiposTarjetasB,
      tarjetasBIds
    );
  }
}
