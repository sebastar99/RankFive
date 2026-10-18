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
