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
@Table(name = "tarjeta_liga")
public class TarjetaLiga extends TarjetaBase {

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
}
