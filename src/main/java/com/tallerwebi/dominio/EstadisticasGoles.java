package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class EstadisticasGoles {

  private EstadisticasGoles() {}

  public static String normalizar(String nombre) {
    if (nombre == null || nombre.isBlank()) {
      return null;
    }
    String limpio = nombre.trim().replaceAll("\\s+", " ");
    return limpio.length() > GolBase.LARGO_NOMBRE
      ? limpio.substring(0, GolBase.LARGO_NOMBRE)
      : limpio;
  }

  public static boolean nombresValidos(String goleador, String asistidor) {
    return goleador != null && !goleador.equalsIgnoreCase(asistidor);
  }

  public static <T extends GolBase> T completar(T gol, String goleador, String asistidor) {
    gol.setGoleador(goleador);
    gol.setAsistidor(asistidor);
    return gol;
  }

  public static int golesDe(Integer goles) {
    return goles == null ? 0 : goles;
  }

  public static List<FilaEstadistica> goleadores(List<? extends GolBase> goles) {
    return ranking(goles, GolBase::getGoleador);
  }

  public static List<FilaEstadistica> asistencias(List<? extends GolBase> goles) {
    return ranking(goles, GolBase::getAsistidor);
  }

  public static <T extends GolBase> Map<Long, List<T>> porPartido(List<T> goles) {
    Map<Long, List<T>> agrupados = new HashMap<>();
    for (T gol : goles) {
      agrupados.computeIfAbsent(gol.getPartidoId(), id -> new ArrayList<>()).add(gol);
    }
    return agrupados;
  }

  public static List<String> nombresDe(Equipo equipo) {
    if (equipo == null) {
      return Collections.emptyList();
    }
    Set<String> nombres = new LinkedHashSet<>();
    agregarNombre(nombres, equipo.getOwner());
    if (equipo.getJugadores() != null) {
      equipo.getJugadores().forEach(j -> agregarNombre(nombres, j));
    }
    return new ArrayList<>(nombres);
  }

  private static List<FilaEstadistica> ranking(
    List<? extends GolBase> goles,
    Function<GolBase, String> jugador
  ) {
    Map<String, FilaEstadistica> filas = new LinkedHashMap<>();
    for (GolBase gol : goles) {
      String nombre = jugador.apply(gol);
      if (nombre == null || nombre.isBlank()) {
        continue;
      }
      String clave = nombre.toLowerCase(Locale.ROOT) + "|" + gol.getEquipoId();
      filas.computeIfAbsent(clave, c -> new FilaEstadistica(nombre, gol.getEquipoNombre())).sumar();
    }
    List<FilaEstadistica> tabla = new ArrayList<>(filas.values());
    tabla.sort(FilaEstadistica.ORDEN);
    return tabla;
  }

  private static void agregarNombre(Set<String> nombres, Usuario usuario) {
    if (usuario == null) {
      return;
    }
    String nombre = usuario.getPerfil() != null ? usuario.getPerfil().getNombreUsuario() : null;
    if (nombre == null || nombre.isBlank()) {
      nombre = usuario.getEmail();
    }
    if (nombre != null && !nombre.isBlank()) {
      nombres.add(nombre);
    }
  }
}
