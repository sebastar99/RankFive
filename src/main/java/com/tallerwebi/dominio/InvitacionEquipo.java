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
@Table(name = "invitacion_equipo")
public class InvitacionEquipo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "equipo_id")
  private Equipo equipo;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoInvitacionEquipo estado = EstadoInvitacionEquipo.PENDIENTE;

  @Column(nullable = false)
  private LocalDateTime fecha = LocalDateTime.now();

  public boolean estaPendiente() {
    return EstadoInvitacionEquipo.PENDIENTE.equals(estado);
  }

  public boolean fueAceptada() {
    return EstadoInvitacionEquipo.ACEPTADA.equals(estado);
  }

  public boolean fueEnviadaA(Long usuarioId) {
    return usuario != null && usuario.getId() != null && usuario.getId().equals(usuarioId);
  }
}
