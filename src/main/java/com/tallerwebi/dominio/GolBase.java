package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_goleador_id")
  private Usuario usuarioGoleador;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_asistente_id")
  private Usuario usuarioAsistente;

  public boolean tieneAsistencia() {
    return asistidor != null && !asistidor.isBlank();
  }

  public abstract Long getPartidoId();

  public abstract Long getEquipoId();

  public abstract String getEquipoNombre();
}
