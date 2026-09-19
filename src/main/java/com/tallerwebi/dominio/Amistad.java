package com.tallerwebi.dominio;

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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "amistad")
public class Amistad {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "amigo_id")
  private Usuario amigo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoAmistad estado = EstadoAmistad.PENDIENTE;

  @Column(nullable = false)
  private LocalDateTime fechaSolicitud = LocalDateTime.now();

  public boolean estaPendiente() {
    return EstadoAmistad.PENDIENTE.equals(estado);
  }

  public boolean fueEnviadaA(Long usuarioId) {
    return amigo != null && amigo.getId() != null && amigo.getId().equals(usuarioId);
  }
}
