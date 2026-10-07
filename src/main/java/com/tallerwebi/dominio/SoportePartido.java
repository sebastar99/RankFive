package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class SoportePartido {

  public List<GolPartido> construirGoles(
    Partido partido,
    List<Long> golesAIds,
    List<Long> asistenciasAIds,
    List<Long> golesBIds,
    List<Long> asistenciasBIds
  ) {
    Map<Long, Usuario> mapa = new HashMap<>();
    for (Usuario u : partido.getEquipoA()) mapa.put(u.getId(), u);
    for (Usuario u : partido.getEquipoB()) mapa.put(u.getId(), u);

    Function<Usuario, String> nombre = u -> {
      if (u == null) return null;
      if (
        u.getPerfil() != null &&
        u.getPerfil().getNombreUsuario() != null &&
        !u.getPerfil().getNombreUsuario().isBlank()
      ) {
        return u.getPerfil().getNombreUsuario();
      }
      return u.getEmail();
    };

    List<GolPartido> out = new ArrayList<>();
    agregarGolesDeEquipo(out, partido, mapa, nombre, golesAIds, asistenciasAIds);
    agregarGolesDeEquipo(out, partido, mapa, nombre, golesBIds, asistenciasBIds);
    return out;
  }

  private static void agregarGolesDeEquipo(
    List<GolPartido> out,
    Partido partido,
    Map<Long, Usuario> mapa,
    Function<Usuario, String> nombre,
    List<Long> golesIds,
    List<Long> asistenciasIds
  ) {
    int golesCount = golesIds == null ? 0 : golesIds.size();
    int asistenciasCount = asistenciasIds == null ? 0 : asistenciasIds.size();
    int filas = Math.max(golesCount, asistenciasCount);
    for (int i = 0; i < filas; i++) {
      Long idGol = (golesIds != null && i < golesCount) ? golesIds.get(i) : null;
      Long idAst = (asistenciasIds != null && i < asistenciasCount) ? asistenciasIds.get(i) : null;
      agregarFilaGol(out, partido, mapa, nombre, idGol, idAst);
    }
  }

  private static void agregarFilaGol(
    List<GolPartido> out,
    Partido partido,
    Map<Long, Usuario> mapa,
    Function<Usuario, String> nombre,
    Long idGoleador,
    Long idAsistidor
  ) {
    if (idGoleador == null) {
      return;
    }
    Usuario goleador = mapa.get(idGoleador);
    if (goleador == null) {
      return;
    }
    Usuario asistidor = idAsistidor == null ? null : mapa.get(idAsistidor);
    GolPartido gol = new GolPartido();
    gol.setPartido(partido);
    gol.setGoleador(nombre.apply(goleador));
    if (asistidor != null) {
      gol.setAsistidor(nombre.apply(asistidor));
    }
    out.add(gol);
  }

  // --- Validaciones/Calculos delegados ---
  public boolean equiposSinSolapamiento(
    java.util.Collection<Usuario> equipoA,
    java.util.Collection<Usuario> equipoB
  ) {
    if (equipoA == null || equipoB == null || equipoA.isEmpty() || equipoB.isEmpty()) {
      return false;
    }
    java.util.Set<Long> ids = new java.util.HashSet<>();
    for (Usuario usuario : equipoA) {
      ids.add(usuario.getId());
    }
    for (Usuario usuario : equipoB) {
      if (ids.contains(usuario.getId())) {
        return false;
      }
    }
    return true;
  }

  public int[] calcularDeltasElo(
    java.util.Collection<Usuario> equipoA,
    java.util.Collection<Usuario> equipoB,
    int golesEquipoA,
    int golesEquipoB,
    int factorK
  ) {
    double promedioA = promedioPl(equipoA);
    double promedioB = promedioPl(equipoB);
    double esperadoA = probabilidadEsperada(promedioA, promedioB);
    double esperadoB = probabilidadEsperada(promedioB, promedioA);
    double resultadoA = resultadoDe(golesEquipoA, golesEquipoB);
    double resultadoB = 1.0 - resultadoA;

    int deltaA = (int) Math.round(factorK * (resultadoA - esperadoA));
    int deltaB = (int) Math.round(factorK * (resultadoB - esperadoB));
    if (golesEquipoA != golesEquipoB && deltaA == 0 && deltaB == 0) {
      if (golesEquipoA > golesEquipoB) {
        deltaA = 1;
        deltaB = -1;
      } else {
        deltaA = -1;
        deltaB = 1;
      }
    }
    return new int[] { deltaA, deltaB };
  }

  public double promedioPl(java.util.Collection<Usuario> equipo) {
    return equipo.stream().mapToInt(j -> j.getPerfil().getPl()).average().orElse(0);
  }

  public double probabilidadEsperada(double promedioPropio, double promedioRival) {
    return 1.0 / (1.0 + Math.pow(10, (promedioRival - promedioPropio) / 400.0));
  }

  public double resultadoDe(int golesA, int golesB) {
    if (golesA > golesB) return 1.0;
    if (golesA < golesB) return 0.0;
    return 0.5;
  }
}
