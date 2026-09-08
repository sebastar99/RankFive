package com.tallerwebi.dominio;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.Period;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class PerfilJugador {

  private String nombreUsuario;
  private String nombreCompleto;
  private LocalDate fechaNacimiento;
  private String lugarResidencia;
  private String posicionPreferida;
  private String piernaHabil;
  private Integer pl = 1000;

  @Transient
  public Integer getEdad() {
    if (fechaNacimiento == null) {
      return null;
    }
    return Period.between(fechaNacimiento, LocalDate.now()).getYears();
  }
}
