package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "partido_liga")
public class PartidoLiga {

  public static final String PENDIENTE = "PENDIENTE";
  public static final String JUGADO = "JUGADO";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "liga_id")
  private Liga liga;

  private Integer fecha;

  @ManyToOne(optional = false)
  @JoinColumn(name = "local_id")
  private InscripcionLiga local;

  @ManyToOne(optional = false)
  @JoinColumn(name = "visitante_id")
  private InscripcionLiga visitante;

  private Integer golesLocal;
  private Integer golesVisitante;

  private String estado = PENDIENTE;

  @ManyToOne
  @JoinColumn(name = "arbitro_id")
  private Arbitro arbitro;

  public boolean estaJugado() {
    return JUGADO.equals(estado);
  }
}
