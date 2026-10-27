package com.tallerwebi.dominio;

import jakarta.persistence.Column;
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
@Table(name = "mensaje")
public class Mensaje {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "emisor_id", nullable = false)
  private Usuario emisor;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "destinatario_id", nullable = false)
  private Usuario destinatario;

  @Column(nullable = false, length = 2000)
  private String contenido;

  @Column(name = "fecha_envio", nullable = false)
  private LocalDateTime fechaEnvio;

  public Mensaje() {}

  public Mensaje(Usuario emisor, Usuario destinatario, String contenido) {
    this.emisor = emisor;
    this.destinatario = destinatario;
    this.contenido = contenido;
    this.fechaEnvio = LocalDateTime.now();
  }
}
