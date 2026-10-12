package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.enums.TipoTarjeta;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SoportePartidoTest {

  private static Usuario u(long id, int pl) {
    Usuario x = new Usuario();
    x.setId(id);
    PerfilJugador pj = new PerfilJugador();
    pj.setPl(pl);
    x.setPerfil(pj);
    return x;
  }

  @Test
  void calcularDeltasElo_victoria_y_derrota_en_PL_igual() {
    SoportePartido sp = new SoportePartido();
    List<Usuario> a = new ArrayList<>();
    List<Usuario> b = new ArrayList<>();
    a.add(u(1L, 1000));
    a.add(u(2L, 1000));
    b.add(u(3L, 1000));
    b.add(u(4L, 1000));

    int[] deltas = sp.calcularDeltasElo(a, b, 1, 0, 32);
    assertArrayEquals(new int[] { 16, -16 }, deltas);

    int[] deltasDerrota = sp.calcularDeltasElo(a, b, 0, 1, 32);
    assertArrayEquals(new int[] { -16, 16 }, deltasDerrota);
  }

  @Test
  void calcularDeltasElo_empate_queda_cero_en_PL_igual() {
    SoportePartido sp = new SoportePartido();
    List<Usuario> a = List.of(u(1L, 1000));
    List<Usuario> b = List.of(u(2L, 1000));

    int[] deltas = sp.calcularDeltasElo(a, b, 0, 0, 32);
    assertArrayEquals(new int[] { 0, 0 }, deltas);
  }

  @Test
  void equiposSinSolapamiento_detecta_colisiones_por_id() {
    SoportePartido sp = new SoportePartido();
    List<Usuario> a = new ArrayList<>();
    List<Usuario> b = new ArrayList<>();
    a.add(u(1L, 1000));
    b.add(u(2L, 1000));
    assertTrue(sp.equiposSinSolapamiento(a, b));

    b.add(u(1L, 1000)); // mismo id en ambos equipos -> invalido
    assertFalse(sp.equiposSinSolapamiento(a, b));
  }

  @Test
  public void seAsociaElGolAlUsuarioGoleador() {
    SoportePartido soportePartido = new SoportePartido();
    Usuario usuario = u(1L, 1000);
    usuario.getPerfil().setNombreUsuario("Seba");
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);

    List<GolPartido> goles = soportePartido.construirGoles(
      partido,
      List.of(usuario.getId()),
      List.of(),
      List.of(),
      List.of()
    );

    assertEquals(1, goles.size());
    assertSame(usuario, goles.get(0).getUsuarioGoleador());
  }

  @Test
  public void seAsociaLaAsistenciaAlUsuarioAsistente() {
    SoportePartido soportePartido = new SoportePartido();
    Usuario usuario = u(1L, 1000);
    usuario.getPerfil().setNombreUsuario("Seba");
    Usuario asistente = u(2L, 1000);
    asistente.getPerfil().setNombreUsuario("Asistente");
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);
    partido.getEquipoA().add(asistente);

    List<GolPartido> goles = soportePartido.construirGoles(
      partido,
      List.of(usuario.getId()),
      List.of(asistente.getId()),
      List.of(),
      List.of()
    );

    assertEquals(1, goles.size());
    assertSame(asistente, goles.get(0).getUsuarioAsistente());
  }

  @Test
  public void seAsociaElGolYLaAsistenciaAlUsuarioGoleadorYAsistente() {
    SoportePartido soportePartido = new SoportePartido();
    Usuario usuario = u(1L, 1000);
    usuario.getPerfil().setNombreUsuario("Seba");
    Usuario asistente = u(2L, 1000);
    asistente.getPerfil().setNombreUsuario("Asistente");
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);
    partido.getEquipoA().add(asistente);

    List<GolPartido> goles = soportePartido.construirGoles(
      partido,
      List.of(usuario.getId()),
      List.of(asistente.getId()),
      List.of(),
      List.of()
    );

    assertEquals(1, goles.size());
    assertSame(usuario, goles.get(0).getUsuarioGoleador());
    assertSame(asistente, goles.get(0).getUsuarioAsistente());
  }

  @Test
  public void sePermiteUnGolSinAsistencia() {
    SoportePartido soportePartido = new SoportePartido();
    Usuario usuario = u(1L, 1000);
    usuario.getPerfil().setNombreUsuario("Seba");
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);

    List<GolPartido> goles = soportePartido.construirGoles(
      partido,
      List.of(usuario.getId()),
      List.of(),
      List.of(),
      List.of()
    );

    assertEquals(1, goles.size());
    assertSame(usuario, goles.get(0).getUsuarioGoleador());
    assertNull(goles.get(0).getUsuarioAsistente());
  }

  @Test
  public void seDebeIdentificarAlGoleadorPorIdAunqueDosUsuariosTenganElMismoNombre() {
    SoportePartido soportePartido = new SoportePartido();
    Usuario usuario1 = u(1L, 1000);
    usuario1.getPerfil().setNombreUsuario("Seba");
    Usuario usuario2 = u(2L, 1000);
    usuario2.getPerfil().setNombreUsuario("Seba");
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario1);
    partido.getEquipoA().add(usuario2);

    List<GolPartido> goles = soportePartido.construirGoles(
      partido,
      List.of(usuario1.getId()),
      List.of(),
      List.of(),
      List.of()
    );

    assertEquals(1, goles.size());
    assertSame(usuario1, goles.get(0).getUsuarioGoleador());
    assertNull(goles.get(0).getUsuarioAsistente());
  }

  @Test
  public void construirTarjetasDeberiaCrearUnaTarjetaPorJugador() {
    SoportePartido soporte = new SoportePartido();
    Partido partido = new Partido();
    Usuario usuario = u(1L, 1000);
    usuario.getPerfil().setNombreUsuario("Seba");
    partido.getEquipoA().add(usuario);

    List<TarjetaPartido> tarjetas = soporte.construirTarjetas(
      partido,
      List.of("AMARILLA"), // tiposTarjetasA
      List.of(1L), // tarjetasAIds
      List.of(), // tiposTarjetasB
      List.of() // tarjetasBIds
    );

    assertEquals(1, tarjetas.size());
    assertSame(usuario, tarjetas.get(0).getUsuarioAmonestado());
    assertEquals("Seba", tarjetas.get(0).getJugador());
    assertEquals(TipoTarjeta.AMARILLA, tarjetas.get(0).getTipo());
  }

  @Test
  public void construirTarjetasDeberiaAsignarElTipoCorrecto() {
    SoportePartido soporte = new SoportePartido();
    Partido partido = new Partido();
    Usuario usuario = u(1L, 1000);
    usuario.getPerfil().setNombreUsuario("Seba");
    partido.getEquipoA().add(usuario);
    Usuario usuarioB = u(2L, 1000);
    usuarioB.getPerfil().setNombreUsuario("Otro");
    partido.getEquipoB().add(usuarioB);

    List<TarjetaPartido> tarjetas = soporte.construirTarjetas(
      partido,
      List.of("ROJA"), // tiposTarjetasA
      List.of(1L), // tarjetasAIds
      List.of("AMARILLA"), // tiposTarjetasB
      List.of(2L) // tarjetasBIds
    );

    assertEquals(2, tarjetas.size());
    assertSame(usuario, tarjetas.get(0).getUsuarioAmonestado());
    assertEquals("Seba", tarjetas.get(0).getJugador());
    assertEquals(TipoTarjeta.ROJA, tarjetas.get(0).getTipo());
    assertEquals(TipoTarjeta.AMARILLA, tarjetas.get(1).getTipo());
  }

  @Test
  public void construirTarjetasDeberiaIgnorarJugadoresQueNoSonDelPartido() {
    SoportePartido soporte = new SoportePartido();
    Partido partido = new Partido();
    Usuario usuario = u(1L, 1000);
    usuario.getPerfil().setNombreUsuario("Seba");
    partido.getEquipoA().add(usuario);
    Usuario usuarioB = u(2L, 1000);
    usuarioB.getPerfil().setNombreUsuario("Otro");
    partido.getEquipoB().add(usuarioB);

    List<TarjetaPartido> tarjetas = soporte.construirTarjetas(
      partido,
      List.of("ROJA"), // tiposTarjetasA
      List.of(1L), // tarjetasAIds
      List.of("AMARILLA"), // tiposTarjetasB
      List.of(3L) // tarjetasBIds
    );

    assertEquals(1, tarjetas.size());
    assertSame(usuario, tarjetas.get(0).getUsuarioAmonestado());
    assertEquals("Seba", tarjetas.get(0).getJugador());
    assertEquals(TipoTarjeta.ROJA, tarjetas.get(0).getTipo());
  }

  @Test
  public void construirTarjetasDeberiaIgnorarFilasSinJugador() {
    Partido partido = new Partido();
    SoportePartido soporte = new SoportePartido();

    List<TarjetaPartido> tarjetas = soporte.construirTarjetas(
      partido,
      List.of("ROJA"), // tiposTarjetasA
      List.of(), // tarjetasAIds
      List.of("AMARILLA"), // tiposTarjetasB
      List.of() // tarjetasBIds
    );

    assertEquals(0, tarjetas.size());
  }

  @Test
  public void construirTarjetasDeberiaFuncionarParaAmbosEquipos() {
    SoportePartido soporte = new SoportePartido();
    Partido partido = new Partido();
    Usuario usuarioA = u(1L, 1000);
    usuarioA.getPerfil().setNombreUsuario("Seba");
    partido.getEquipoA().add(usuarioA);
    Usuario usuarioB = u(2L, 1000);
    usuarioB.getPerfil().setNombreUsuario("Otro");
    partido.getEquipoB().add(usuarioB);

    List<TarjetaPartido> tarjetas = soporte.construirTarjetas(
      partido,
      List.of("ROJA"), // tiposTarjetasA
      List.of(1L), // tarjetasAIds
      List.of("AMARILLA"), // tiposTarjetasB
      List.of(2L) // tarjetasBIds
    );

    assertEquals(2, tarjetas.size());
  }
}
