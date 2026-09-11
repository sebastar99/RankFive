package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class PartidoTest {

  @Test
  public void unPartidoRecienCreadoNoDeberiaTenerResultadoCargado() {
    Partido partido = new Partido();

    assertThat(partido.fueJugado(), is(false));
  }

  @Test
  public void registrarResultadoDeberiaGuardarLosGolesDeCadaEquipo() {
    Partido partido = new Partido();

    partido.registrarResultado(3, 1);

    assertThat(partido.getGolesEquipoA(), equalTo(3));
    assertThat(partido.getGolesEquipoB(), equalTo(1));
  }

  @Test
  public void registrarResultadoDeberiaMarcarElPartidoComoJugado() {
    Partido partido = new Partido();

    partido.registrarResultado(2, 2);

    assertThat(partido.fueJugado(), is(true));
  }
}
