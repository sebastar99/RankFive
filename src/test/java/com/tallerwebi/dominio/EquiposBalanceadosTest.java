package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class EquiposBalanceadosTest {

  private static Usuario u(long id, int pl) {
    Usuario x = new Usuario();
    x.setId(id);
    PerfilJugador pj = new PerfilJugador();
    pj.setPl(pl);
    x.setPerfil(pj);
    return x;
  }

  @Test
  public void elTotalDeCadaEquipoEsLaSumaDeSusPL() {
    List<Usuario> equipoA = new ArrayList<>();
    List<Usuario> equipoB = new ArrayList<>();
    equipoA.add(u(1, 1200));
    equipoB.add(u(2, 1500));
    equipoA.add(u(3, 1000));

    EquiposBalanceados equipo = new EquiposBalanceados(equipoA, equipoB);

    assertThat(equipo.getTotalA(), equalTo(2200));
    assertThat(equipo.getTotalB(), equalTo(1500));
  }

  @Test
  public void laDiferenciaEsSiemprePositiva() {
    List<Usuario> equipoA = new ArrayList<>();
    List<Usuario> equipoB = new ArrayList<>();

    equipoA.add(u(1, 1000));
    equipoB.add(u(2, 1500));

    EquiposBalanceados resultado = new EquiposBalanceados(equipoA, equipoB);

    assertThat(resultado.getDiferencia(), equalTo(500));
  }
}
