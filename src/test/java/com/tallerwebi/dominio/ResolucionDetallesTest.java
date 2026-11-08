package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.enums.TipoTarjeta;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResolucionDetallesTest {

  private Equipo local;
  private Equipo visitante;
  private Usuario delantero;
  private Usuario enganche;
  private Usuario rival;

  @BeforeEach
  public void init() {
    delantero = usuario(1L, "delantero");
    enganche = usuario(2L, "enganche");
    rival = usuario(9L, "rival");
    local = equipo(delantero, enganche);
    visitante = equipo(rival);
  }

  @Test
  public void resuelveGolesAsistenciasYTarjetasDeCadaLado() {
    DetallesEncuentro detalles = new DetallesEncuentro(
      List.of(1L),
      List.of(2L),
      List.of("AMARILLA"),
      List.of(1L),
      List.of(9L),
      List.of(),
      List.of("ROJA"),
      List.of(9L)
    );

    Optional<ResolucionDetalles> resolucion = ResolucionDetalles.de(
      detalles,
      local,
      visitante,
      1,
      1
    );

    assertThat(resolucion.isPresent(), equalTo(true));
    ResolucionDetalles r = resolucion.get();
    assertThat(r.golesLocal().get(0).goleador(), equalTo(delantero));
    assertThat(r.golesLocal().get(0).asistidor(), equalTo(enganche));
    assertThat(r.golesVisitante().get(0).goleador(), equalTo(rival));
    assertThat(r.tarjetasLocal().get(0).tipo(), equalTo(TipoTarjeta.AMARILLA));
    assertThat(r.tarjetasVisitante().get(0).tipo(), equalTo(TipoTarjeta.ROJA));
  }

  @Test
  public void esVaciaSiFaltanDatosOEquipos() {
    assertThat(ResolucionDetalles.de(null, local, visitante, 1, 1).isEmpty(), equalTo(true));
    DetallesEncuentro detalles = detallesVacias();
    assertThat(ResolucionDetalles.de(detalles, null, visitante, 1, 1).isEmpty(), equalTo(true));
    assertThat(ResolucionDetalles.de(detalles, local, null, 1, 1).isEmpty(), equalTo(true));
  }

  @Test
  public void fallaCuandoLosGolesSuperanElMarcadorDeCualquierLado() {
    DetallesEncuentro excedeLocal = new DetallesEncuentro(
      List.of(1L, 1L),
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
    assertThat(ResolucionDetalles.de(excedeLocal, local, visitante, 1, 3).isEmpty(), equalTo(true));

    DetallesEncuentro excedeVisitante = new DetallesEncuentro(
      null,
      null,
      null,
      null,
      List.of(9L, 9L, 9L),
      null,
      null,
      null
    );
    assertThat(
      ResolucionDetalles.de(excedeVisitante, local, visitante, 1, 2).isEmpty(),
      equalTo(true)
    );
  }

  @Test
  public void filtraIdsDeJugadoresAjenosYNulos() {
    DetallesEncuentro detalles = new DetallesEncuentro(
      Arrays.asList(1L, 9L, null, 77L),
      null,
      null,
      Arrays.asList(9L, 1L),
      null,
      null,
      null,
      null
    );

    ResolucionDetalles r = ResolucionDetalles.de(detalles, local, visitante, 3, 0).get();

    assertThat(r.golesLocal(), hasSize(1));
    assertThat(r.tarjetasLocal(), hasSize(1));
    assertThat(r.tarjetasLocal().get(0).jugador(), equalTo(delantero));
  }

  @Test
  public void laAsistenciaIgualAlGoleadorQuedaNula() {
    DetallesEncuentro detalles = new DetallesEncuentro(
      List.of(1L),
      List.of(1L),
      null,
      null,
      null,
      null,
      null,
      null
    );

    ResolucionDetalles r = ResolucionDetalles.de(detalles, local, visitante, 1, 0).get();

    assertThat(r.golesLocal().get(0).asistidor(), nullValue());
  }

  @Test
  public void lasTarjetasSinJugadorOTipoInvalidoNoSeGuardan() {
    DetallesEncuentro detalles = new DetallesEncuentro(
      null,
      null,
      Arrays.asList("AMARILLA", null),
      Arrays.asList(null, 1L),
      null,
      null,
      null,
      null
    );

    ResolucionDetalles r = ResolucionDetalles.de(detalles, local, visitante, 0, 0).get();

    assertThat(r.golesLocal(), empty());
    assertThat(r.tarjetasLocal(), hasSize(1));
    assertThat(r.tarjetasLocal().get(0).tipo(), equalTo(TipoTarjeta.AMARILLA));
  }

  private DetallesEncuentro detallesVacias() {
    return new DetallesEncuentro(null, null, null, null, null, null, null, null);
  }

  private static Equipo equipo(Usuario... jugadores) {
    Equipo equipo = new Equipo();
    equipo.setOwner(jugadores[0]);
    for (Usuario jugador : jugadores) {
      equipo.agregarJugador(jugador);
    }
    return equipo;
  }

  private static Usuario usuario(Long id, String nombre) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.getPerfil().setNombreUsuario(nombre);
    usuario.setEmail(nombre + "@mail.com");
    return usuario;
  }
}
