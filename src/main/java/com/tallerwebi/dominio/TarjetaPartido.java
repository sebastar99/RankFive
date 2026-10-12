package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.TipoTarjeta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "tarjeta_partido")
public class TarjetaPartido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "partido_id")
  private Partido partido;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_amonestado_id")
  private Usuario usuarioAmonestado;

  @Column(nullable = false, length = 60)
  private String jugador;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private TipoTarjeta tipo;

  public boolean esAmarilla() {
    return tipo == TipoTarjeta.AMARILLA;
  }

  public boolean esRoja() {
    return tipo == TipoTarjeta.ROJA;
  }
}