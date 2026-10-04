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
@Table(name = "gol_torneo")
public class GolTorneo extends GolBase {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "encuentro_id")
  private EncuentroTorneo encuentro;

  @ManyToOne(optional = false)
  @JoinColumn(name = "inscripcion_id")
  private InscripcionTorneo inscripcion;

  @Override
  public Long getPartidoId() {
    return encuentro == null ? null : encuentro.getId();
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
