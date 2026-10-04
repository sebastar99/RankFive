package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "gol_liga")
public class GolLiga extends GolBase {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "partido_id")
  private PartidoLiga partido;

  @ManyToOne(optional = false)
  @JoinColumn(name = "inscripcion_id")
  private InscripcionLiga inscripcion;

  @Override
  public Long getPartidoId() {
    return partido == null ? null : partido.getId();
  }

  @Override
  public Long getEquipoId() {
    return inscripcion == null ? null : inscripcion.getId();
  }

  @Override
  public String getEquipoNombre() {
    return inscripcion == null ? null : inscripcion.getNombreVisible();
  }
}
