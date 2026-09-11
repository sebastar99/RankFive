package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "partido")
public class Partido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "partido_equipo_a",
    joinColumns = @JoinColumn(name = "partido_id"),
    inverseJoinColumns = @JoinColumn(name = "usuario_id")
  )
  private Set<Usuario> equipoA = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "partido_equipo_b",
    joinColumns = @JoinColumn(name = "partido_id"),
    inverseJoinColumns = @JoinColumn(name = "usuario_id")
  )
  private Set<Usuario> equipoB = new HashSet<>();

  private LocalDateTime fecha = LocalDateTime.now();

  private Integer golesEquipoA;
  private Integer golesEquipoB;

  public boolean fueJugado() {
    return golesEquipoA != null && golesEquipoB != null;
  }

  public void registrarResultado(int golesA, int golesB) {
    this.golesEquipoA = golesA;
    this.golesEquipoB = golesB;
  }
}
