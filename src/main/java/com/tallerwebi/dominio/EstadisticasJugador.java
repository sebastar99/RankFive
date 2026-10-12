package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class EstadisticasJugador {

  private static final String ENTERO_EN_CERO = "INT DEFAULT 0";

  @Column(columnDefinition = ENTERO_EN_CERO)
  private Integer partidosJugados = 0;

  @Column(columnDefinition = ENTERO_EN_CERO)
  private Integer goles = 0;

  @Column(columnDefinition = ENTERO_EN_CERO)
  private Integer asistencias = 0;

  @Column(columnDefinition = "DOUBLE DEFAULT 0")
  private Double puntaje = 0.0;

  @Column(columnDefinition = ENTERO_EN_CERO)
  private Integer partidosGanados = 0;

  @Column(columnDefinition = ENTERO_EN_CERO)
  private Integer partidosEmpatados = 0;

  @Column(columnDefinition = ENTERO_EN_CERO)
  private Integer partidosPerdidos = 0;

  @Column(columnDefinition = ENTERO_EN_CERO)
  private Integer tarjetasAmarillas = 0;

  @Column(columnDefinition = ENTERO_EN_CERO)
  private Integer tarjetasRojas = 0;

  public double getPromedioGolesPorPartido() {
    if (partidosJugados == 0) {
      return 0.0;
    }
    return (double) goles / partidosJugados;
  }

  public double getPromedioAsistenciasPorPartido() {
    if (partidosJugados == 0) {
      return 0.0;
    }
    return (double) asistencias / partidosJugados;
  }
}
