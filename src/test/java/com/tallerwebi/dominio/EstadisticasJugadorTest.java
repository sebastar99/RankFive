package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EstadisticasJugadorTest {

  @Test
  public void calcularGolesPorPartidoConResultadoEntero() {
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setGoles(10);
    estadisticas.setPartidosJugados(5);

    double promedioDeGoles = estadisticas.getPromedioGolesPorPartido();
    assertEquals(2.0, promedioDeGoles);
  }

  @Test
  public void calcularGolesPorPartidoConResultadoDecimal() {
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setGoles(7);
    estadisticas.setPartidosJugados(3);

    double promedioDeGoles = estadisticas.getPromedioGolesPorPartido();
    assertEquals(2.33, promedioDeGoles, 0.01);
  }

  @Test
  public void calcularGolesPorPartidoConResultadoCero() {
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setGoles(0);
    estadisticas.setPartidosJugados(5);

    double promedioDeGoles = estadisticas.getPromedioGolesPorPartido();
    assertEquals(0.0, promedioDeGoles);
  }

  @Test
  public void calcularAsistenciasPorPartidoConResultadoEntero() {
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setAsistencias(10);
    estadisticas.setPartidosJugados(5);

    double promedioDeAsistencias = estadisticas.getPromedioAsistenciasPorPartido();
    assertEquals(2.0, promedioDeAsistencias);
  }

  @Test
  public void calcularAsistenciasPorPartidoConResultadoDecimal() {
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setAsistencias(7);
    estadisticas.setPartidosJugados(3);

    double promedioDeAsistencias = estadisticas.getPromedioAsistenciasPorPartido();
    assertEquals(2.33, promedioDeAsistencias, 0.01);
  }

  @Test
  public void calcularAsistenciasPorPartidoConResultadoCero() {
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setAsistencias(0);
    estadisticas.setPartidosJugados(5);

    double promedioDeAsistencias = estadisticas.getPromedioAsistenciasPorPartido();
    assertEquals(0.0, promedioDeAsistencias);
  }

  @Test
  public void calcularGolesPorPartidoCuandoNoSeJugaronPartidos() {
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setGoles(0);
    estadisticas.setPartidosJugados(0);

    double promedioDeGoles = estadisticas.getPromedioGolesPorPartido();
    assertEquals(0.0, promedioDeGoles);
  }

  @Test
  public void calcularAsistenciasPorPartidoCuandoNoSeJugaronPartidos() {
    EstadisticasJugador estadisticas = new EstadisticasJugador();
    estadisticas.setAsistencias(0);
    estadisticas.setPartidosJugados(0);

    double promedioDeAsistencias = estadisticas.getPromedioAsistenciasPorPartido();
    assertEquals(0.0, promedioDeAsistencias);
  }
}
