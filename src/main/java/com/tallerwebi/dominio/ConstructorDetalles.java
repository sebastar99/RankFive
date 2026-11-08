package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.TipoTarjeta;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConstructorDetalles {

  public record GolCargado(Usuario goleador, Usuario asistidor) {}

  public record TarjetaCargada(Usuario jugador, TipoTarjeta tipo) {}

  private ConstructorDetalles() {}

  public static List<GolCargado> goles(
    Collection<Usuario> plantel,
    List<Long> golesIds,
    List<Long> asistenciasIds
  ) {
    Map<Long, Usuario> mapa = mapaPorId(plantel);
    List<GolCargado> goles = new ArrayList<>();
    for (int i = 0; i < tamano(golesIds); i++) {
      Usuario goleador = mapa.get(golesIds.get(i));
      Usuario asistidor = i < tamano(asistenciasIds) ? mapa.get(asistenciasIds.get(i)) : null;
      if (goleador != null) {
        boolean seAsisteASiMismo = asistidor != null && goleador.getId().equals(asistidor.getId());
        goles.add(new GolCargado(goleador, seAsisteASiMismo ? null : asistidor));
      }
    }
    return goles;
  }

  public static List<TarjetaCargada> tarjetas(
    Collection<Usuario> plantel,
    List<String> tipos,
    List<Long> jugadoresIds
  ) {
    Map<Long, Usuario> mapa = mapaPorId(plantel);
    List<TarjetaCargada> tarjetas = new ArrayList<>();
    for (int i = 0; i < tamano(jugadoresIds); i++) {
      Usuario jugador = mapa.get(jugadoresIds.get(i));
      if (jugador != null) {
        String tipo = i < tamano(tipos) ? tipos.get(i) : null;
        tarjetas.add(new TarjetaCargada(jugador, parsearTipo(tipo)));
      }
    }
    return tarjetas;
  }

  public static Collection<Usuario> plantelDe(Equipo equipo) {
    Map<Long, Usuario> plantel = new HashMap<>();
    if (equipo != null) {
      equipo.getJugadores().forEach(j -> plantel.put(j.getId(), j));
      if (equipo.getOwner() != null) {
        plantel.putIfAbsent(equipo.getOwner().getId(), equipo.getOwner());
      }
    }
    return plantel.values();
  }

  public static String nombreVisible(Usuario usuario) {
    if (usuario == null) {
      return null;
    }
    String nombre = usuario.getPerfil() == null ? null : usuario.getPerfil().getNombreUsuario();
    return nombre == null || nombre.isBlank() ? usuario.getEmail() : nombre;
  }

  private static Map<Long, Usuario> mapaPorId(Collection<Usuario> plantel) {
    Map<Long, Usuario> mapa = new HashMap<>();
    if (plantel != null) {
      plantel.forEach(u -> mapa.put(u.getId(), u));
    }
    return mapa;
  }

  private static int tamano(List<?> lista) {
    return lista == null ? 0 : lista.size();
  }

  private static TipoTarjeta parsearTipo(String tipo) {
    return TipoTarjeta.ROJA.name().equalsIgnoreCase(tipo) ? TipoTarjeta.ROJA : TipoTarjeta.AMARILLA;
  }
}
