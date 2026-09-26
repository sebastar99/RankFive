package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "torneo")
public class Torneo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String descripcion;
  private Integer formato; // jugadores por equipo: 5,7,8,9,11
  private Integer cupoEquipos;
  private Boolean inscripcionAbierta = Boolean.TRUE;
  private LocalDate fechaInicio;
  private String ubicacion;
}
