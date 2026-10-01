package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inscripcion_torneo")
public class InscripcionTorneo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "torneo_id", nullable = false)
  private Torneo torneo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "equipo_id")
  private Equipo equipo;

  private String nombreEquipo;

  @Column(length = 1)
  private String grupo;

  private LocalDateTime fechaInscripcion = LocalDateTime.now();
}
