package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class EstadisticasJugador {

  @Column(columnDefinition = "INT DEFAULT 0")
  private Integer partidosJugados = 0;

  @Column(columnDefinition = "INT DEFAULT 0")
  private Integer goles = 0;

  @Column(columnDefinition = "INT DEFAULT 0")
  private Integer asistencias = 0;

  @Column(columnDefinition = "DOUBLE DEFAULT 0")
  private Double puntaje = 0.0;
}
