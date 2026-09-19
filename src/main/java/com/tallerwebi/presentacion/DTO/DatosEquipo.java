package com.tallerwebi.presentacion.DTO;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatosEquipo {

  private String nombre;
  private Integer cantidad = 5;
  private List<String> jugadores = new ArrayList<>();
}
