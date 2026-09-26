package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import org.junit.jupiter.api.Test;

public class TablaPosicionesTest {

  @Test
  public void sumaPuntosYOrdenaPorPuntosDiferenciaYGoles() {
    InscripcionTorneo a = inscripcion(1L, "Alfa");
    InscripcionTorneo b = inscripcion(2L, "Beta");
    InscripcionTorneo c = inscripcion(3L, "Gamma");
    FaseTorneo fase = new FaseTorneo();
    fase.getEncuentros().add(encuentro(a, b, 3, 1, "JUGADO"));
    fase.getEncuentros().add(encuentro(c, b, 2, 2, "JUGADO"));
    fase.getEncuentros().add(encuentro(a, c, null, null, "PENDIENTE"));

    List<FilaPosicion> tabla = TablaPosiciones.calcular(List.of(a, b, c), List.of(fase));

    assertThat(tabla, hasSize(3));
    FilaPosicion primero = tabla.get(0);
    assertThat(primero.getNombreEquipo(), equalTo("Alfa"));
    assertThat(primero.getPuntos(), equalTo(3));
    assertThat(primero.getDiferencia(), equalTo(2));
    assertThat(tabla.get(1).getNombreEquipo(), equalTo("Gamma"));
    assertThat(tabla.get(1).getEmpatados(), equalTo(1));
    FilaPosicion ultimo = tabla.get(2);
    assertThat(ultimo.getNombreEquipo(), equalTo("Beta"));
    assertThat(ultimo.getJugados(), equalTo(2));
    assertThat(ultimo.getPerdidos(), equalTo(1));
    assertThat(ultimo.getGolesContra(), equalTo(5));
  }

  @Test
  public void usaNombreDelEquipoOElUsuarioSiNoHayNombre() {
    InscripcionTorneo conEquipo = inscripcion(1L, null);
    Equipo equipo = new Equipo();
    equipo.setNombre("Los Pibes");
    conEquipo.setEquipo(equipo);
    InscripcionTorneo sinNombre = inscripcion(2L, null);

    List<FilaPosicion> tabla = TablaPosiciones.calcular(List.of(conEquipo, sinNombre), List.of());

    assertThat(tabla.get(0).getNombreEquipo(), equalTo("Los Pibes"));
    assertThat(tabla.get(1).getNombreEquipo(), equalTo("user2"));
  }

  private static InscripcionTorneo inscripcion(Long id, String nombre) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.getPerfil().setNombreUsuario("user" + id);
    InscripcionTorneo insc = new InscripcionTorneo();
    insc.setId(id);
    insc.setUsuario(usuario);
    insc.setNombreEquipo(nombre);
    return insc;
  }

  private static EncuentroTorneo encuentro(
    InscripcionTorneo a,
    InscripcionTorneo b,
    Integer golesA,
    Integer golesB,
    String estado
  ) {
    EncuentroTorneo encuentro = new EncuentroTorneo();
    encuentro.setParticipanteA(a);
    encuentro.setParticipanteB(b);
    encuentro.setGolesA(golesA);
    encuentro.setGolesB(golesB);
    encuentro.setEstado(estado);
    return encuentro;
  }
}
