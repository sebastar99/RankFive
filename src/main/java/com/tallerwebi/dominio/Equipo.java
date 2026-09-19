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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "equipo")
public class Equipo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 40)
  private String nombre;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private Usuario owner;

  @Column(nullable = false)
  private Integer formato;

  @Column(length = 255)
  private String fotoUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoEquipo estado = EstadoEquipo.PENDIENTE;

  @Column(nullable = false)
  private LocalDateTime fechaCreacion = LocalDateTime.now();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "equipo_jugador",
    joinColumns = @JoinColumn(name = "equipo_id"),
    inverseJoinColumns = @JoinColumn(name = "usuario_id")
  )
  private Set<Usuario> jugadores = new HashSet<>();

  public boolean estaActivo() {
    return EstadoEquipo.ACTIVO.equals(estado);
  }

  public void agregarJugador(Usuario jugador) {
    if (jugador != null) {
      jugadores.add(jugador);
    }
  }

  public int getPromedioPl() {
    if (jugadores == null || jugadores.isEmpty()) {
      return 0;
    }
    int suma = 0;
    for (Usuario jugador : jugadores) {
      suma += plDe(jugador);
    }
    return Math.round((float) suma / jugadores.size());
  }

  public RangoEquipo getRango() {
    return RangoEquipo.dePromedio(getPromedioPl());
  }

  private static int plDe(Usuario usuario) {
    if (usuario.getPerfil() == null || usuario.getPerfil().getPl() == null) {
      return 0;
    }
    return usuario.getPerfil().getPl();
  }
}
