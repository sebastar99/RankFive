package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Arbitro;
import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.EstadisticasGoles;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.GolLiga;
import com.tallerwebi.dominio.GolTorneo;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.PartidoLiga;
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
public class DetalleCompetenciaVistaTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;
  private Usuario admin;
  private Arbitro arbitro;

  @BeforeEach
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    admin = usuario("admin@vista.com", "ADMIN");
    arbitro = new Arbitro();
    arbitro.setNombre("Pitana");
    sesion().persist(arbitro);
  }

  @Test
  public void detalleDeLigaRenderizaFixtureEstadisticasYCalificacion() throws Exception {
    Liga liga = new Liga();
    liga.setNombre("Liga Vista");
    liga.setFormato(5);
    liga.setCupoEquipos(4);
    liga.setFechaInicio(LocalDate.now());
    liga.setUbicacion("CABA");
    sesion().persist(liga);
    InscripcionLiga local = inscripcionLiga(liga, admin, "Locales FC");
    InscripcionLiga visitante = inscripcionLiga(
      liga,
      usuario("rival@vista.com", "USER"),
      "Visitantes FC"
    );
    PartidoLiga jugado = partidoLiga(liga, 1, local, visitante, PartidoLiga.JUGADO);
    partidoLiga(liga, 2, visitante, local, PartidoLiga.PENDIENTE);
    GolLiga gol = EstadisticasGoles.completar(new GolLiga(), "Goleador Uno", "Asistidor Uno");
    gol.setPartido(jugado);
    gol.setInscripcion(local);
    sesion().persist(gol);
    sesion().flush();

    String html = renderizar("/ligas/detalle?id=" + liga.getId());

    assertThat(html, containsString("fecha-pill"));
    assertThat(html, containsString("Tabla de goleadores"));
    assertThat(html, containsString("Goleador Uno"));
    assertThat(html, containsString("Asistidor Uno"));
    assertThat(html, containsString("/ligas/partido/calificar-arbitro"));
    assertThat(html, containsString("/ligas/partido/resultado"));
    assertThat(html, containsString("tipo=liga"));
    assertThat(html, containsString("/js/fixture.js"));
  }

  @Test
  public void detalleDeTorneoUsaElMismoFormatoQueLaLiga() throws Exception {
    Torneo torneo = new Torneo();
    torneo.setNombre("Copa Vista");
    torneo.setFormato(5);
    torneo.setCupoEquipos(4);
    torneo.setInscripcionAbierta(false);
    torneo.setFechaInicio(LocalDate.now());
    torneo.setUbicacion("CABA");
    sesion().persist(torneo);
    InscripcionTorneo alfa = inscripcionTorneo(torneo, admin, "Alfa FC");
    InscripcionTorneo beta = inscripcionTorneo(
      torneo,
      usuario("beta@vista.com", "USER"),
      "Beta FC"
    );
    FaseTorneo grupos = fase(torneo, 1, "Fase de grupos", FaseTorneo.TIPO_GRUPOS);
    EncuentroTorneo jugado = encuentro(grupos, alfa, beta, "JUGADO");
    FaseTorneo finalFase = fase(torneo, 2, "Final", FaseTorneo.TIPO_ELIMINACION);
    encuentro(finalFase, alfa, beta, "PENDIENTE");
    GolTorneo gol = EstadisticasGoles.completar(new GolTorneo(), "Goleador Copa", null);
    gol.setEncuentro(jugado);
    gol.setInscripcion(alfa);
    sesion().persist(gol);
    sesion().flush();
    sesion().clear();

    String html = renderizar("/torneos/detalle?id=" + torneo.getId());

    assertThat(html, containsString("tab-estadisticas"));
    assertThat(html, containsString("tab-arbitros"));
    assertThat(html, containsString("fecha-pill"));
    assertThat(html, containsString("Fase de grupos"));
    assertThat(html, containsString("Goleador Copa"));
    assertThat(html, containsString("Goles por equipo"));
    assertThat(html, containsString("/torneos/encuentro/calificar-arbitro"));
    assertThat(html, containsString("tipo=torneo"));
    assertThat(html, not(containsString("Árbitros designados")));
  }

  private String renderizar(String url) throws Exception {
    return mockMvc
      .perform(get(url).sessionAttr("usuario", admin))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private Session sesion() {
    return sessionFactory.getCurrentSession();
  }

  private Usuario usuario(String email, String rol) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("123");
    usuario.setRol(rol);
    usuario.getPerfil().setNombreUsuario(email.substring(0, email.indexOf('@')));
    sesion().persist(usuario);
    return usuario;
  }

  private InscripcionLiga inscripcionLiga(Liga liga, Usuario usuario, String nombre) {
    InscripcionLiga insc = new InscripcionLiga();
    insc.setLiga(liga);
    insc.setUsuario(usuario);
    insc.setNombreEquipo(nombre);
    sesion().persist(insc);
    return insc;
  }

  private PartidoLiga partidoLiga(
    Liga liga,
    int fecha,
    InscripcionLiga local,
    InscripcionLiga visitante,
    String estado
  ) {
    PartidoLiga partido = new PartidoLiga();
    partido.setLiga(liga);
    partido.setFecha(fecha);
    partido.setLocal(local);
    partido.setVisitante(visitante);
    partido.setArbitro(arbitro);
    partido.setEstado(estado);
    if (PartidoLiga.JUGADO.equals(estado)) {
      partido.setGolesLocal(2);
      partido.setGolesVisitante(0);
    }
    sesion().persist(partido);
    return partido;
  }

  private InscripcionTorneo inscripcionTorneo(Torneo torneo, Usuario usuario, String nombre) {
    InscripcionTorneo insc = new InscripcionTorneo();
    insc.setTorneo(torneo);
    insc.setUsuario(usuario);
    insc.setNombreEquipo(nombre);
    insc.setGrupo("A");
    sesion().persist(insc);
    return insc;
  }

  private FaseTorneo fase(Torneo torneo, int numero, String nombre, String tipo) {
    FaseTorneo fase = new FaseTorneo();
    fase.setTorneo(torneo);
    fase.setNumero(numero);
    fase.setNombre(nombre);
    fase.setTipo(tipo);
    sesion().persist(fase);
    return fase;
  }

  private EncuentroTorneo encuentro(
    FaseTorneo fase,
    InscripcionTorneo a,
    InscripcionTorneo b,
    String estado
  ) {
    EncuentroTorneo encuentro = new EncuentroTorneo();
    encuentro.setFase(fase);
    encuentro.setParticipanteA(a);
    encuentro.setParticipanteB(b);
    encuentro.setGrupo(fase.esDeGrupos() ? "A" : null);
    encuentro.setArbitro(arbitro);
    encuentro.setEstado(estado);
    if ("JUGADO".equals(estado)) {
      encuentro.setGolesA(1);
      encuentro.setGolesB(0);
    }
    sesion().persist(encuentro);
    fase.getEncuentros().add(encuentro);
    return encuentro;
  }
}
