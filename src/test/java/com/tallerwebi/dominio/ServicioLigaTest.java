package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioLigaImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioLigaTest {

  private RepositorioLiga repoLiga;
  private RepositorioInscripcionLiga repoInscripcion;
  private RepositorioPartidoLiga repoPartido;
  private ServicioEquipo servicioEquipo;
  private ServicioLigaImpl servicio;
  private Liga liga;
  private Usuario usuario;

  @BeforeEach
  public void init() {
    repoLiga = mock(RepositorioLiga.class);
    repoInscripcion = mock(RepositorioInscripcionLiga.class);
    repoPartido = mock(RepositorioPartidoLiga.class);
    servicioEquipo = mock(ServicioEquipo.class);
    ServicioArbitro servicioArbitro = mock(ServicioArbitro.class);
    Arbitro arbitro = new Arbitro();
    arbitro.setNombre("Pitana");
    when(servicioArbitro.listarRanking()).thenReturn(List.of(arbitro));
    servicio =
      new ServicioLigaImpl(repoLiga, repoInscripcion, repoPartido, servicioEquipo, servicioArbitro);

    liga = new Liga();
    liga.setId(5L);
    liga.setCupoEquipos(18);
    liga.setInscripcionAbierta(true);
    when(repoLiga.buscarPorId(5L)).thenReturn(liga);
    usuario = new Usuario();
    usuario.setId(1L);
  }

  @Test
  public void inscribeConEquipoActivoYRechazaSinEquipo() {
    Equipo equipo = new Equipo();
    equipo.setNombre("Los Pibes");
    when(servicioEquipo.listarEquiposActivosDe(usuario)).thenReturn(List.of(equipo));

    assertThat(servicio.inscribir(usuario, 5L), equalTo(ResultadoInscripcion.INSCRIPTO));
    verify(repoInscripcion).guardar(any(InscripcionLiga.class));

    when(servicioEquipo.listarEquiposActivosDe(usuario)).thenReturn(Collections.emptyList());
    assertThat(servicio.inscribir(usuario, 5L), equalTo(ResultadoInscripcion.SIN_EQUIPO_ACTIVO));
  }

  @Test
  public void noInscribeSiYaEstaInscriptoOSiLaLigaEstaCerrada() {
    when(repoInscripcion.existePara(1L, 5L)).thenReturn(true);
    assertThat(servicio.inscribir(usuario, 5L), equalTo(ResultadoInscripcion.YA_INSCRIPTO));

    liga.setInscripcionAbierta(false);
    assertThat(servicio.inscribir(usuario, 5L), equalTo(ResultadoInscripcion.INSCRIPCION_CERRADA));
    verify(repoInscripcion, never()).guardar(any(InscripcionLiga.class));
  }

  @Test
  public void generarFixtureCreaPartidosDeIdaYVuelta() {
    when(repoInscripcion.listarPorLiga(5L)).thenReturn(inscripciones(4));

    boolean ok = servicio.generarFixtureSiNoExiste(5L);

    assertThat(ok, is(true));
    verify(repoPartido, times(12)).guardar(any(PartidoLiga.class));
  }

  @Test
  public void tablaSumaResultadosDePartidosJugados() {
    List<InscripcionLiga> equipos = inscripciones(2);
    PartidoLiga partido = new PartidoLiga();
    partido.setId(9L);
    partido.setFecha(1);
    partido.setLocal(equipos.get(0));
    partido.setVisitante(equipos.get(1));
    when(repoInscripcion.listarPorLiga(5L)).thenReturn(equipos);
    when(repoPartido.buscarPorId(9L)).thenReturn(partido);
    when(repoPartido.listarPorLiga(5L)).thenReturn(List.of(partido));

    assertThat(servicio.registrarResultado(9L, 3, 1), is(true));
    assertThat(servicio.registrarResultado(9L, 1, 1), is(false));

    List<FilaPosicion> tabla = servicio.calcularTabla(5L);
    assertThat(tabla.get(0).getNombreEquipo(), equalTo("Equipo 1"));
    assertThat(tabla.get(0).getPuntos(), equalTo(3));
    assertThat(servicio.obtenerFechas(5L).get(1), hasSize(1));
  }

  private static List<InscripcionLiga> inscripciones(int cantidad) {
    List<InscripcionLiga> lista = new ArrayList<>();
    for (long i = 1; i <= cantidad; i++) {
      InscripcionLiga insc = new InscripcionLiga();
      insc.setId(i);
      insc.setNombreEquipo("Equipo " + i);
      lista.add(insc);
    }
    return lista;
  }
}
