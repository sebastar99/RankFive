package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GolPartidoTest {

  @Test
  void getters_basicos_funcionan() {
    Partido p = new Partido();
    p.setId(99L);

    GolPartido g = new GolPartido();
    g.setPartido(p);
    g.setGoleador("userA");
    g.setAsistidor("userB");

    assertEquals(99L, g.getPartidoId());
    assertNull(g.getEquipoId());
    assertNull(g.getEquipoNombre());
  }
}
