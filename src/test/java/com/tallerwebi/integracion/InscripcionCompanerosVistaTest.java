package com.tallerwebi.integracion;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadoEquipo;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
@Transactional
public class InscripcionCompanerosVistaTest {

  @Autowired
  private WebApplicationContext contexto;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;
  private Usuario capitan;
  private Usuario companiero;
  private Equipo equipo;
  private Torneo torneo;
  private Liga liga;

  @BeforeEach
  public void preparar() {
    mockMvc = MockMvcBuilders.webAppContextSetup(requireNonNull(contexto)).build();
    capitan = usuario("capitan");
    companiero = usuario("companiero");
    equipo = new Equipo();
    equipo.setNombre("SacaPuntos FC");
    equipo.setFormato(5);
    equipo.setEstado(EstadoEquipo.ACTIVO);
    equipo.setOwner(capitan);
    equipo.agregarJugador(capitan);
    equipo.agregarJugador(companiero);
    sesion().persist(equipo);

    torneo = new Torneo();
    torneo.setNombre("Copa del plantel");
    torneo.setFormato(5);
    torneo.setCupoEquipos(16);
    torneo.setInscripcionAbierta(true);
    torneo.setFechaInicio(LocalDate.now());
    torneo.setUbicacion("CABA");
    sesion().persist(torneo);
    InscripcionTorneo inscTorneo = new InscripcionTorneo();
    inscTorneo.setTorneo(torneo);
    inscTorneo.setUsuario(capitan);
    inscTorneo.setEquipo(equipo);
    inscTorneo.setNombreEquipo(equipo.getNombre());
    inscTorneo.setGrupo("A");
    sesion().persist(inscTorneo);

    liga = new Liga();
    liga.setNombre("Liga del plantel");
    liga.setFormato(5);
    liga.setCupoEquipos(18);
    liga.setInscripcionAbierta(true);
    liga.setFechaInicio(LocalDate.now());
    liga.setUbicacion("CABA");
    sesion().persist(liga);
    InscripcionLiga inscLiga = new InscripcionLiga();
    inscLiga.setLiga(liga);
    inscLiga.setUsuario(capitan);
    inscLiga.setEquipo(equipo);
    inscLiga.setNombreEquipo(equipo.getNombre());
    sesion().persist(inscLiga);
    sesion().flush();
    sesion().clear();
  }

  @Test
  public void elCompanieroVeYaInscriptoEnElListadoDeTorneosYLigas() throws Exception {
    String html = html(get("/torneos"), companiero);

    assertThat(html, containsString("Ya inscripto"));
    assertThat(html, not(containsString("action=\"/torneos/inscribirse\"")));
    assertThat(html, not(containsString("action=\"/ligas/inscribirse\"")));
    assertThat(html, containsString("/torneos/detalle?id=" + torneo.getId()));
    assertThat(html, containsString("/ligas/detalle?id=" + liga.getId()));
  }

  @Test
  public void elCompanieroVeYaInscriptoEnLosDetallesYLaInformacion() throws Exception {
    String detalleTorneo = html(get("/torneos/detalle?id=" + torneo.getId()), companiero);
    String detalleLiga = html(get("/ligas/detalle?id=" + liga.getId()), companiero);

    assertThat(detalleTorneo, containsString("Ya inscripto"));
    assertThat(detalleTorneo, not(containsString("action=\"/torneos/inscribirse\"")));
    assertThat(detalleTorneo, containsString("SacaPuntos FC"));
    assertThat(detalleLiga, containsString("Ya inscripto"));
    assertThat(detalleLiga, not(containsString("action=\"/ligas/inscribirse\"")));
    assertThat(detalleLiga, containsString("SacaPuntos FC"));
  }

  @Test
  public void elCompanieroNoPuedeInscribirDeNuevoElEquipoNiEnTorneoNiEnLiga() throws Exception {
    String urlTorneo = mockMvc
      .perform(
        post("/torneos/inscribirse")
          .sessionAttr("usuario", requireNonNull(companiero))
          .param("torneoId", torneo.getId().toString())
      )
      .andExpect(status().is3xxRedirection())
      .andReturn()
      .getResponse()
      .getRedirectedUrl();

    String urlLiga = mockMvc
      .perform(
        post("/ligas/inscribirse")
          .sessionAttr("usuario", requireNonNull(companiero))
          .param("ligaId", liga.getId().toString())
      )
      .andExpect(status().is3xxRedirection())
      .andReturn()
      .getResponse()
      .getRedirectedUrl();

    assertThat(requireNonNull(urlTorneo), containsString("YA_INSCRIPTO"));
    assertThat(requireNonNull(urlLiga), containsString("YA_INSCRIPTO"));
    assertEquals(1L, cantidad("InscripcionTorneo"));
    assertEquals(1L, cantidad("InscripcionLiga"));
  }

  @Test
  public void rechazaEquipoF5EnTorneoF8SinGuardarInscripcion() throws Exception {
    Torneo futbol8 = torneoAbierto(8);

    String destino = inscribir(futbol8, equipo.getId());

    assertThat(destino, containsString("SIN_EQUIPO_COMPATIBLE"));
    assertEquals(1L, cantidad("InscripcionTorneo"));
  }

  @Test
  public void exigeElegirCuandoHayDosEquiposCompatibles() throws Exception {
    Torneo futbol5 = torneoAbierto(5);
    equipo("Otro F5", 5, capitan, EstadoEquipo.ACTIVO);

    String destino = inscribir(futbol5, null);

    assertThat(destino, containsString("SELECCIONAR_EQUIPO"));
    assertEquals(1L, cantidad("InscripcionTorneo"));
  }

  @Test
  public void inscribeElEquipoElegidoAunqueNoSeaElPrimeroDeLaLista() throws Exception {
    Torneo futbol5 = torneoAbierto(5);
    equipo("Mas reciente F5", 5, capitan, EstadoEquipo.ACTIVO);

    String destino = inscribir(futbol5, equipo.getId());

    assertThat(destino, containsString("aviso=INSCRIPTO"));
    InscripcionTorneo guardada = sesion()
      .createQuery("from InscripcionTorneo i where i.torneo.id = :id", InscripcionTorneo.class)
      .setParameter("id", futbol5.getId())
      .getSingleResult();
    assertEquals(equipo.getId(), guardada.getEquipo().getId());
    assertEquals(equipo.getNombre(), guardada.getNombreEquipo());
  }

  @Test
  public void rechazaEquipoAjenoPendienteODeOtroFormatoAunqueHayaUnoValido() throws Exception {
    Torneo futbol5 = torneoAbierto(5);
    Equipo ajeno = equipo("Ajeno F5", 5, usuario("otroCapitan"), EstadoEquipo.ACTIVO);
    Equipo pendiente = equipo("Pendiente F5", 5, capitan, EstadoEquipo.PENDIENTE);
    Equipo futbol8 = equipo("Propio F8", 8, capitan, EstadoEquipo.ACTIVO);

    for (Long id : java.util.List.of(ajeno.getId(), pendiente.getId(), futbol8.getId(), -1L)) {
      assertThat(inscribir(futbol5, id), containsString("EQUIPO_NO_DISPONIBLE"));
    }
    assertEquals(1L, cantidad("InscripcionTorneo"));
  }

  @Test
  public void listadoYDetallePermitenElegirSoloEquiposActivosDelMismoFormato() throws Exception {
    Torneo futbol5 = torneoAbierto(5);
    equipo("Alternativo F5", 5, capitan, EstadoEquipo.ACTIVO);
    equipo("Incompatible F8", 8, capitan, EstadoEquipo.ACTIVO);
    equipo("Pendiente F5", 5, capitan, EstadoEquipo.PENDIENTE);
    equipo("Ajeno F5", 5, usuario("otroCapitan"), EstadoEquipo.ACTIVO);

    for (String ruta : java.util.List.of("/torneos", "/torneos/detalle?id=" + futbol5.getId())) {
      String contenido = html(get(ruta), capitan);

      assertThat(contenido, containsString("name=\"equipoId\""));
      assertThat(contenido, containsString("SacaPuntos FC</option>"));
      assertThat(contenido, containsString("Alternativo F5</option>"));
      assertThat(contenido, not(containsString("Incompatible F8</option>")));
      assertThat(contenido, not(containsString("Pendiente F5</option>")));
      assertThat(contenido, not(containsString("Ajeno F5</option>")));
    }
  }

  @Test
  public void sinEquipoCompatibleNoOfreceInscripcionEnListadoNiDetalle() throws Exception {
    Torneo futbol8 = torneoAbierto(8);

    for (String ruta : java.util.List.of("/torneos", "/torneos/detalle?id=" + futbol8.getId())) {
      String contenido = html(get(ruta), capitan);

      assertThat(contenido, not(containsString("action=\"/torneos/inscribirse\"")));
      assertThat(contenido.replaceAll("<[^>]*>", ""), containsString("equipo activo de F8"));
    }
  }

  @Test
  public void conUnSoloEquipoCompatibleConservaLaInscripcionSinSeleccionExplicita()
    throws Exception {
    Torneo futbol5 = torneoAbierto(5);

    assertThat(inscribir(futbol5, null), containsString("aviso=INSCRIPTO"));
    assertEquals(2L, cantidad("InscripcionTorneo"));
  }

  private String inscribir(Torneo destino, Long equipoId) throws Exception {
    var peticion = post("/torneos/inscribirse")
      .sessionAttr("usuario", requireNonNull(capitan))
      .param("torneoId", destino.getId().toString());
    if (equipoId != null) {
      peticion.param("equipoId", equipoId.toString());
    }
    return requireNonNull(
      mockMvc
        .perform(peticion)
        .andExpect(status().is3xxRedirection())
        .andReturn()
        .getResponse()
        .getRedirectedUrl()
    );
  }

  @Test
  public void rechazaEquipoF5EnLigaF8SinGuardarInscripcion() throws Exception {
    Liga futbol8 = ligaAbierta(8);

    String destino = inscribirLiga(futbol8, equipo.getId());

    assertThat(destino, containsString("SIN_EQUIPO_COMPATIBLE"));
    assertEquals(1L, cantidad("InscripcionLiga"));
  }

  @Test
  public void ligaExigeElegirCuandoHayDosEquiposCompatiblesYPersisteElElegido() throws Exception {
    Liga futbol5 = ligaAbierta(5);
    equipo("Otro F5", 5, capitan, EstadoEquipo.ACTIVO);

    assertThat(inscribirLiga(futbol5, null), containsString("SELECCIONAR_EQUIPO"));
    assertThat(inscribirLiga(futbol5, equipo.getId()), containsString("aviso=INSCRIPTO"));

    InscripcionLiga guardada = sesion()
      .createQuery("from InscripcionLiga i where i.liga.id = :id", InscripcionLiga.class)
      .setParameter("id", futbol5.getId())
      .getSingleResult();
    assertEquals(equipo.getId(), guardada.getEquipo().getId());
    assertEquals(equipo.getNombre(), guardada.getNombreEquipo());
  }

  @Test
  public void ligaRechazaEquipoAjenoPendienteODeOtroFormato() throws Exception {
    Liga futbol5 = ligaAbierta(5);
    Equipo ajeno = equipo("Ajeno F5", 5, usuario("otroCapitan"), EstadoEquipo.ACTIVO);
    Equipo pendiente = equipo("Pendiente F5", 5, capitan, EstadoEquipo.PENDIENTE);
    Equipo futbol8 = equipo("Propio F8", 8, capitan, EstadoEquipo.ACTIVO);

    for (Long id : java.util.List.of(ajeno.getId(), pendiente.getId(), futbol8.getId(), -1L)) {
      assertThat(inscribirLiga(futbol5, id), containsString("EQUIPO_NO_DISPONIBLE"));
    }
    assertEquals(1L, cantidad("InscripcionLiga"));
  }

  @Test
  public void listadoYDetalleDeLigaPermitenElegirSoloEquiposActivosDelMismoFormato()
    throws Exception {
    Liga futbol5 = ligaAbierta(5);
    equipo("Alternativo F5", 5, capitan, EstadoEquipo.ACTIVO);
    equipo("Incompatible F8", 8, capitan, EstadoEquipo.ACTIVO);
    equipo("Pendiente F5", 5, capitan, EstadoEquipo.PENDIENTE);
    equipo("Ajeno F5", 5, usuario("otroCapitan"), EstadoEquipo.ACTIVO);

    for (String ruta : java.util.List.of("/torneos", "/ligas/detalle?id=" + futbol5.getId())) {
      String contenido = html(get(requireNonNull(ruta)), capitan);

      assertThat(contenido, containsString("name=\"equipoId\""));
      assertThat(contenido, containsString("Alternativo F5</option>"));
      assertThat(contenido, not(containsString("Incompatible F8</option>")));
      assertThat(contenido, not(containsString("Pendiente F5</option>")));
      assertThat(contenido, not(containsString("Ajeno F5</option>")));
    }
  }

  @Test
  public void sinEquipoCompatibleNoOfreceInscripcionEnLiga() throws Exception {
    Liga futbol8 = ligaAbierta(8);

    for (String ruta : java.util.List.of("/torneos", "/ligas/detalle?id=" + futbol8.getId())) {
      String contenido = html(get(requireNonNull(ruta)), capitan);

      assertThat(contenido, not(containsString("action=\"/ligas/inscribirse\"")));
      assertThat(contenido.replaceAll("<[^>]*>", ""), containsString("equipo activo de F8"));
    }
  }

  private String inscribirLiga(Liga destino, Long equipoId) throws Exception {
    var peticion = post("/ligas/inscribirse")
      .sessionAttr("usuario", requireNonNull(capitan))
      .param("ligaId", destino.getId().toString());
    if (equipoId != null) {
      peticion.param("equipoId", equipoId.toString());
    }
    return requireNonNull(
      mockMvc
        .perform(peticion)
        .andExpect(status().is3xxRedirection())
        .andReturn()
        .getResponse()
        .getRedirectedUrl()
    );
  }

  private Liga ligaAbierta(int formato) {
    Liga nueva = new Liga();
    nueva.setNombre("Liga F" + formato);
    nueva.setFormato(formato);
    nueva.setCupoEquipos(18);
    nueva.setInscripcionAbierta(true);
    nueva.setFechaInicio(LocalDate.now());
    sesion().persist(nueva);
    return nueva;
  }

  private Torneo torneoAbierto(int formato) {
    Torneo nuevo = new Torneo();
    nuevo.setNombre("Copa F" + formato);
    nuevo.setFormato(formato);
    nuevo.setCupoEquipos(16);
    nuevo.setInscripcionAbierta(true);
    nuevo.setFechaInicio(LocalDate.now());
    sesion().persist(nuevo);
    return nuevo;
  }

  private Equipo equipo(String nombre, int formato, Usuario owner, EstadoEquipo estado) {
    Equipo nuevo = new Equipo();
    nuevo.setNombre(nombre);
    nuevo.setFormato(formato);
    nuevo.setEstado(estado);
    nuevo.setOwner(owner);
    nuevo.setFechaCreacion(LocalDateTime.now().plusDays(1));
    nuevo.agregarJugador(owner);
    sesion().persist(nuevo);
    return nuevo;
  }

  private String html(
    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder peticion,
    Usuario usuario
  ) throws Exception {
    return mockMvc
      .perform(peticion.sessionAttr("usuario", requireNonNull(usuario)))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
  }

  private long cantidad(String entidad) {
    return sesion()
      .createQuery("select count(i.id) from " + entidad + " i", Long.class)
      .getSingleResult();
  }

  private Usuario usuario(String nombre) {
    Usuario usuario = new Usuario();
    usuario.setEmail(nombre + "@test.com");
    usuario.setPassword("123");
    usuario.setRol("USER");
    usuario.getPerfil().setNombreUsuario(nombre);
    sesion().persist(usuario);
    return usuario;
  }

  private Session sesion() {
    return sessionFactory.getCurrentSession();
  }
}
