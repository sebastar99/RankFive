package com.tallerwebi.presentacion.DTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.PerfilJugador;
import com.tallerwebi.dominio.RangoEquipo;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.junit.jupiter.api.Test;

class DatosEquipoVistaTest {

  @Test
  void proyectaElEquipoYOrdenaLosJugadoresPorPuntaje() {
    Usuario puntajeAlto = jugadorConPuntaje(1800);
    Usuario puntajeBajo = jugadorConPuntaje(1100);
    Usuario sinPerfil = new Usuario();
    sinPerfil.setPerfil(null);
    Usuario sinPuntaje = jugadorConPuntaje(null);

    Equipo equipo = new Equipo();
    equipo.setId(3L);
    equipo.setNombre("Las estrellas");
    equipo.setFotoUrl("/equipo.png");
    equipo.setFormato(5);
    equipo.agregarJugador(puntajeBajo);
    equipo.agregarJugador(sinPerfil);
    equipo.agregarJugador(sinPuntaje);
    equipo.agregarJugador(puntajeAlto);

    DatosEquipoVista vista = new DatosEquipoVista(equipo);

    assertEquals(3L, vista.getId());
    assertEquals("Las estrellas", vista.getNombre());
    assertEquals("/equipo.png", vista.getFotoUrl());
    assertEquals(5, vista.getFormato());
    assertEquals(725, vista.getPromedioPl());
    assertEquals(RangoEquipo.PRINCIPIANTE, vista.getRango());
    assertEquals(RangoEquipo.assetPath(RangoEquipo.PRINCIPIANTE), vista.getRangoIcono());
    assertEquals(puntajeAlto, vista.getJugadores().get(0));
    assertEquals(puntajeBajo, vista.getJugadores().get(1));
    assertTrue(vista.getJugadores().subList(2, 4).containsAll(List.of(sinPerfil, sinPuntaje)));
  }

  @Test
  void convierteListasDeEquiposYConservaNulosDelModelo() {
    Equipo equipo = new Equipo();
    equipo.setNombre("Sin rango");
    DatosEquipoVista vista = DatosEquipoVista.desde(List.of(equipo)).get(0);

    assertEquals("Sin rango", vista.getNombre());
    assertEquals(RangoEquipo.PRINCIPIANTE, vista.getRango());
    assertEquals(RangoEquipo.assetPath(RangoEquipo.PRINCIPIANTE), vista.getRangoIcono());
    assertEquals(List.of(), DatosEquipoVista.desde(List.of()).stream().toList());
  }

  private static Usuario jugadorConPuntaje(Integer puntaje) {
    Usuario usuario = new Usuario();
    PerfilJugador perfil = new PerfilJugador();
    perfil.setPl(puntaje);
    usuario.setPerfil(perfil);
    return usuario;
  }
}
