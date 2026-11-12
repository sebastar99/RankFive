package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioInscripcionesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioInscripcionesTest {

  private RepositorioTorneo repoTorneo;
  private RepositorioLiga repoLiga;
  private ServicioInscripciones servicio;

  @BeforeEach
  public void init() {
    repoTorneo = mock(RepositorioTorneo.class);
    repoLiga = mock(RepositorioLiga.class);
    servicio = new ServicioInscripcionesImpl(repoTorneo, repoLiga);
  }

  @Test
  public void abrirInscripcionTorneoMarcaLaInscripcionComoAbierta() {
    Torneo torneo = new Torneo();
    torneo.setInscripcionAbierta(false);
    when(repoTorneo.buscarPorId(1L)).thenReturn(torneo);

    assertTrue(servicio.abrirInscripcionTorneo(1L));
    assertTrue(torneo.getInscripcionAbierta());
  }

  @Test
  public void abrirInscripcionTorneoDevuelveFalseSiYaEstabaAbiertaONoExiste() {
    Torneo torneo = new Torneo();
    torneo.setInscripcionAbierta(true);
    when(repoTorneo.buscarPorId(1L)).thenReturn(torneo);

    assertFalse(servicio.abrirInscripcionTorneo(1L));
    assertFalse(servicio.abrirInscripcionTorneo(2L));
    assertFalse(servicio.abrirInscripcionTorneo(null));
  }

  @Test
  public void abrirInscripcionLigaMarcaLaInscripcionComoAbierta() {
    Liga liga = new Liga();
    liga.setInscripcionAbierta(false);
    when(repoLiga.buscarPorId(1L)).thenReturn(liga);

    assertTrue(servicio.abrirInscripcionLiga(1L));
    assertTrue(liga.getInscripcionAbierta());
  }

  @Test
  public void abrirInscripcionLigaDevuelveFalseSiYaEstabaAbiertaONoExiste() {
    Liga liga = new Liga();
    liga.setInscripcionAbierta(true);
    when(repoLiga.buscarPorId(1L)).thenReturn(liga);

    assertFalse(servicio.abrirInscripcionLiga(1L));
    assertFalse(servicio.abrirInscripcionLiga(2L));
    assertFalse(servicio.abrirInscripcionLiga(null));
  }

  @Test
  public void laEntidadNoDejaReabrirUnaInscripcionYaAbierta() {
    Torneo torneo = new Torneo();
    torneo.setInscripcionAbierta(false);
    Liga liga = new Liga();
    liga.setInscripcionAbierta(true);

    assertTrue(torneo.abrirInscripcion());
    assertFalse(torneo.abrirInscripcion());
    assertFalse(liga.abrirInscripcion());
    assertEquals(Boolean.TRUE, liga.getInscripcionAbierta());
  }
}
