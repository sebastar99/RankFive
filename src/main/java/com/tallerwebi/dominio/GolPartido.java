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
@Table(name = "gol_partido")
public class GolPartido extends GolBase {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "partido_id")
  private Partido partido;

  @Override
  public Long getPartidoId() {
    return partido == null ? null : partido.getId();
  }

  @Override
  public Long getEquipoId() {
    return null; // En partidos libres no hay entidad "equipo" persistida
  }

  @Override
  public String getEquipoNombre() {
    return null;
  }
}
