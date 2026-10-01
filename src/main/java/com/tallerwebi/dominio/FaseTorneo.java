package com.tallerwebi.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fase_torneo")
public class FaseTorneo {

  public static final String TIPO_GRUPOS = "GRUPOS";
  public static final String TIPO_ELIMINACION = "ELIMINACION";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "torneo_id")
  private Torneo torneo;

  private Integer numero; // 1,2,3...
  private String nombre; // Cuartos, Semis, etc.

  private String tipo = TIPO_ELIMINACION;

  @OneToMany(mappedBy = "fase", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<EncuentroTorneo> encuentros = new ArrayList<>();

  public boolean esDeGrupos() {
    return TIPO_GRUPOS.equals(tipo);
  }
}
