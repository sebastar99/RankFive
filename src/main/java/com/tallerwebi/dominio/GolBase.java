package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class GolBase {

  public static final int LARGO_NOMBRE = 60;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = LARGO_NOMBRE)
  private String goleador;

  @Column(length = LARGO_NOMBRE)
  private String asistidor;

  public boolean tieneAsistencia() {
    return asistidor != null && !asistidor.isBlank();
  }

  public abstract Long getPartidoId();

  public abstract Long getEquipoId();

  public abstract String getEquipoNombre();
}
