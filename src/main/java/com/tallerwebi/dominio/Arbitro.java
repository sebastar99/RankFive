package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "arbitro")
public class Arbitro {

  public static final int PUNTAJE_MINIMO = 1;
  public static final int PUNTAJE_MAXIMO = 5;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 60)
  private String nombre;

  @Column(nullable = false)
  private Integer pl = 0;

  @Column(nullable = false)
  private Integer puntajeTotal = 0;

  @Column(nullable = false)
  private Integer cantidadCalificaciones = 0;

  public double getPromedio() {
    if (cantidadCalificaciones == null || cantidadCalificaciones == 0) {
      return 0.0;
    }
    return Math.round((puntajeTotal * 10.0) / cantidadCalificaciones) / 10.0;
  }

  public void calificar(int puntaje) {
    if (puntaje < PUNTAJE_MINIMO || puntaje > PUNTAJE_MAXIMO) {
      return;
    }
    puntajeTotal = (puntajeTotal == null ? 0 : puntajeTotal) + puntaje;
    cantidadCalificaciones = (cantidadCalificaciones == null ? 0 : cantidadCalificaciones) + 1;
  }
}
