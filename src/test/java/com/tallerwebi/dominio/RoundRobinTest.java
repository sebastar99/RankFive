package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class RoundRobinTest {

  @Test
  public void dieciochoEquiposIdaYVueltaJuegan34FechasY306Partidos() {
    List<Integer> equipos = IntStream.rangeClosed(1, 18).boxed().collect(Collectors.toList());

    List<List<Cruce<Integer>>> fechas = RoundRobin.idaYVuelta(equipos);

    assertThat(fechas, hasSize(34));
    int total = fechas.stream().mapToInt(List::size).sum();
    assertThat(total, equalTo(306));
    Map<String, Integer> localias = new HashMap<>();
    for (List<Cruce<Integer>> fecha : fechas) {
      assertThat(fecha, hasSize(9));
      for (Cruce<Integer> cruce : fecha) {
        localias.merge(cruce.getLocal() + "-" + cruce.getVisitante(), 1, Integer::sum);
      }
    }
    assertThat(localias.size(), equalTo(306));
  }

  @Test
  public void cantidadImparDejaUnEquipoLibrePorFecha() {
    List<List<Cruce<String>>> fechas = RoundRobin.soloIda(List.of("A", "B", "C"));

    assertThat(fechas, hasSize(3));
    assertThat(fechas.get(0), hasSize(1));
    assertThat(RoundRobin.soloIda(List.of("A")), hasSize(0));
  }
}
