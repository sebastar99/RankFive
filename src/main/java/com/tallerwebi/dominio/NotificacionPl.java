package com.tallerwebi.dominio;

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
@Table(name = "notificacion_pl")
public class NotificacionPl {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "partido_id")
  private Partido partido;

  private Integer delta;

  private Boolean leida = false;

  private LocalDateTime fecha = LocalDateTime.now();

  public String getMensaje() {
    int valor = delta == null ? 0 : delta;
    return (valor >= 0 ? "+" : "") + valor + " PL";
  }
}
