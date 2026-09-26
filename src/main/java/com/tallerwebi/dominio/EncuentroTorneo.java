package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "encuentro_torneo")
public class EncuentroTorneo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "fase_id")
  private FaseTorneo fase;

  @ManyToOne
  @JoinColumn(name = "participante_a_id")
  private InscripcionTorneo participanteA;

  @ManyToOne
  @JoinColumn(name = "participante_b_id")
  private InscripcionTorneo participanteB;

  private Integer golesA;
  private Integer golesB;

  private String estado; // PENDIENTE, BYE, JUGADO

  private String grupo;

  @ManyToOne
  @JoinColumn(name = "ganador_id")
  private InscripcionTorneo ganador;

  @ManyToOne
  @JoinColumn(name = "arbitro_id")
  private Arbitro arbitro;
}
