package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class BalanceadorEquiposTest {

  private static Usuario u(long id, int pl) {
    Usuario x = new Usuario();
    x.setId(id);
    PerfilJugador pj = new PerfilJugador();
    pj.setPl(pl);
    x.setPerfil(pj);
    return x;
  }

  @Test
  public void conDosJugadoresCadaEquipoTieneUno() {
    BalanceadorEquipos balanceador = new BalanceadorEquipos();
    List<Usuario> jugadores = new ArrayList<>();
    jugadores.add(u(1, 1200));
    jugadores.add(u(2, 1000));

    EquiposBalanceados resultado = balanceador.armarEquipos(jugadores);

    assertThat(resultado.getEquipoA(), hasSize(1));
    assertThat(resultado.getEquipoB(), hasSize(1));
  }

  @Test
  public void conCuatroJugadoresCadaEquipoTieneDos() {
    BalanceadorEquipos balanceador = new BalanceadorEquipos();
    List<Usuario> jugadores = new ArrayList<>();
    jugadores.add(u(1, 1200));
    jugadores.add(u(2, 1000));
    jugadores.add(u(3, 2000));
    jugadores.add(u(4, 3000));

    EquiposBalanceados resultado = balanceador.armarEquipos(jugadores);

    assertThat(resultado.getEquipoA(), hasSize(2));
    assertThat(resultado.getEquipoB(), hasSize(2));
  }

  @Test
  public void conCuatroJugadoresLosTotalesQuedanParejos() {
    BalanceadorEquipos balanceador = new BalanceadorEquipos();
    List<Usuario> jugadores = new ArrayList<>();
    jugadores.add(u(1, 1200));
    jugadores.add(u(2, 1400));
    jugadores.add(u(3, 1100));
    jugadores.add(u(4, 1300));

    EquiposBalanceados resultado = balanceador.armarEquipos(jugadores);

    assertThat(resultado.getTotalA(), equalTo(2500));
    assertThat(resultado.getTotalB(), equalTo(2500));
  }

  @Test
  public void losEquiposNuncaTienenDistintaCantidadDeJugadores() {
    BalanceadorEquipos balanceador = new BalanceadorEquipos();
    List<Usuario> jugadores = new ArrayList<>();
    jugadores.add(u(1, 3000));
    jugadores.add(u(2, 1000));
    jugadores.add(u(3, 1000));
    jugadores.add(u(4, 1000));

    EquiposBalanceados resultado = balanceador.armarEquipos(jugadores);

    assertThat(resultado.getEquipoA(), hasSize(2));
    assertThat(resultado.getEquipoB(), hasSize(2));
  }

  @Test
  public void con6jugadoresSiBEstaLlenoElRestoVaAlEquipoA() {
    BalanceadorEquipos balanceador = new BalanceadorEquipos();
    List<Usuario> jugadores = new ArrayList<>();
    jugadores.add(u(1, 3000));
    jugadores.add(u(2, 2800));
    jugadores.add(u(3, 2600));
    jugadores.add(u(4, 1000));
    jugadores.add(u(5, 1000));
    jugadores.add(u(6, 1000));

    EquiposBalanceados resultado = balanceador.armarEquipos(jugadores);

    assertThat(resultado.getEquipoA(), hasSize(3));
    assertThat(resultado.getEquipoB(), hasSize(3));
  }

  @Test
  public void conDiezJugadoresLaDiferenciaEsMinima() {
    BalanceadorEquipos balanceador = new BalanceadorEquipos();

    List<Usuario> jugadores = new ArrayList<>();

    jugadores.add(u(1, 1800));
    jugadores.add(u(2, 1650));
    jugadores.add(u(3, 1500));
    jugadores.add(u(4, 1420));
    jugadores.add(u(5, 1300));
    jugadores.add(u(6, 1250));
    jugadores.add(u(7, 1180));
    jugadores.add(u(8, 1100));
    jugadores.add(u(9, 1050));
    jugadores.add(u(10, 1000));

    EquiposBalanceados resultado = balanceador.armarEquipos(jugadores);

    assertThat(resultado.getEquipoA(), hasSize(5));
    assertThat(resultado.getEquipoB(), hasSize(5));
    assertThat(resultado.getDiferencia(), lessThanOrEqualTo(50));
  }
}
