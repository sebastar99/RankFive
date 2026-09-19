package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.RangoEquipo;
import com.tallerwebi.dominio.Usuario;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
public class DatosEquipoVista {

  private final Long id;
  private final String nombre;
  private final String fotoUrl;
  private final Integer formato;
  private final int promedioPl;
  private final RangoEquipo rango;
  private final String rangoIcono;
  private final List<Usuario> jugadores;

  public DatosEquipoVista(Equipo equipo) {
    this.id = equipo.getId();
    this.nombre = equipo.getNombre();
    this.fotoUrl = equipo.getFotoUrl();
    this.formato = equipo.getFormato();
    this.promedioPl = equipo.getPromedioPl();
    this.rango = equipo.getRango();
    this.rangoIcono = RangoEquipo.assetPath(this.rango);
    this.jugadores =
      equipo
        .getJugadores()
        .stream()
        .sorted(Comparator.comparingInt(DatosEquipoVista::plDe).reversed())
        .toList();
  }

  public static List<DatosEquipoVista> desde(List<Equipo> equipos) {
    return equipos.stream().map(DatosEquipoVista::new).toList();
  }

  private static int plDe(Usuario usuario) {
    if (usuario.getPerfil() == null || usuario.getPerfil().getPl() == null) {
      return 0;
    }
    return usuario.getPerfil().getPl();
  }
}
