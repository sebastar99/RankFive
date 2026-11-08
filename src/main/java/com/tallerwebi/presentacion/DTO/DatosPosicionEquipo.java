package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.PosicionEquipo;
import java.util.List;
import lombok.Getter;

@Getter
public class DatosPosicionEquipo {

  private final int posicion;
  private final DatosEquipoVista equipo;
  private final boolean propio;

  public DatosPosicionEquipo(PosicionEquipo posicion) {
    this.posicion = posicion.posicion();
    this.equipo = new DatosEquipoVista(posicion.equipo());
    this.propio = posicion.propio();
  }

  public static List<DatosPosicionEquipo> desde(List<PosicionEquipo> posiciones) {
    return posiciones.stream().map(DatosPosicionEquipo::new).toList();
  }
}
