package com.tallerwebi.integracion;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadoEquipo;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.nio.charset.StandardCharsets;
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
public class EstadisticasEquipoVistaTest {

  @Autowired
  private WebApplicationContext contexto;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;
  private Usuario capitan;
  private Equipo activo;
  private Equipo rival;

  @BeforeEach
  public void preparar() {
    mockMvc = MockMvcBuilders.webAppContextSetup(requireNonNull(contexto)).build();
    capitan = usuario("capitan");
    activo = equipo("Los Estadistas", capitan, EstadoEquipo.ACTIVO);
    rival = equipo("Los Rivales", usuario("rival"), EstadoEquipo.ACTIVO);
  }

  @Test
  public void muestraElRecordOficialDeLigaYTorneoDeCadaEquipoActivo() throws Exception {
    Liga liga = liga();
    InscripcionLiga propiaLiga = inscripcionLiga(liga, capitan, activo);
    InscripcionLiga rivalLiga = inscripcionLiga(liga, rival.getOwner(), rival);
    partidoLiga(liga, propiaLiga, rivalLiga, 3, 1);
    Torneo torneo = torneo();
    InscripcionTorneo propioTorneo = inscripcionTorneo(torneo, capitan, activo);
    InscripcionTorneo rivalTorneo = inscripcionTorneo(torneo, rival.getOwner(), rival);
    encuentro(torneo, propioTorneo, rivalTorneo, 1, 1);

    String contenido = html();

    assertThat(contenido, containsString("Mis equipos"));
    assertThat(contenido, containsString("Los Estadistas"));
    assertThat(contenido, containsString("1G · 1E · 0P"));
    assertThat(contenido, containsString("GF 4 · GC 2"));
    assertThat(contenido, containsString("4 pts"));
    assertThat(contenido, not(containsString("Los Rivales")));
    assertThat(contenido, not(containsString("Sin actividad oficial")));
  }

  @Test
  public void equipoSinPartidosOficialesApareceConCerosYAvisoDeSinActividad() throws Exception {
    String contenido = html();

    assertThat(contenido, containsString("Los Estadistas"));
    assertThat(contenido, containsString("0G · 0E · 0P"));
    assertThat(contenido, containsString("Sin actividad oficial"));
  }

  @Test
  public void usuarioSinEquiposVeEstadoVacioConLinkParaCrearUno() throws Exception {
    sesion().flush();
    sesion().clear();
    Usuario sinEquipo = usuario("solitario");
    sesion().flush();

    String contenido = mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", sinEquipo))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString(StandardCharsets.UTF_8);

    assertThat(contenido, containsString("Mis equipos"));
    assertThat(contenido, containsString("/agregar-equipo"));
    assertThat(contenido, not(containsString("Los Estadistas")));
  }

  private String html() throws Exception {
    sesion().flush();
    sesion().clear();
    return mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", capitan))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString(StandardCharsets.UTF_8);
  }

  private Liga liga() {
    Liga liga = new Liga();
    liga.setNombre("Liga de prueba");
    liga.setFormato(5);
    liga.setCupoEquipos(10);
    liga.setFechaInicio(LocalDate.now());
    sesion().persist(liga);
    return liga;
  }

  private InscripcionLiga inscripcionLiga(Liga liga, Usuario usuario, Equipo equipo) {
    InscripcionLiga insc = new InscripcionLiga();
    insc.setLiga(liga);
    insc.setUsuario(usuario);
    insc.setEquipo(equipo);
    insc.setNombreEquipo(equipo.getNombre());
    sesion().persist(insc);
    return insc;
  }

  private void partidoLiga(
    Liga liga,
    InscripcionLiga local,
    InscripcionLiga visitante,
    int gl,
    int gv
  ) {
    PartidoLiga partido = new PartidoLiga();
    partido.setLiga(liga);
    partido.setFecha(1);
    partido.setLocal(local);
    partido.setVisitante(visitante);
    partido.setGolesLocal(gl);
    partido.setGolesVisitante(gv);
    partido.setEstado(PartidoLiga.JUGADO);
    sesion().persist(partido);
  }

  private Torneo torneo() {
    Torneo torneo = new Torneo();
    torneo.setNombre("Torneo de prueba");
    torneo.setFormato(5);
    torneo.setCupoEquipos(16);
    torneo.setFechaInicio(LocalDate.now());
    sesion().persist(torneo);
    return torneo;
  }

  private InscripcionTorneo inscripcionTorneo(Torneo torneo, Usuario usuario, Equipo equipo) {
    InscripcionTorneo insc = new InscripcionTorneo();
    insc.setTorneo(torneo);
    insc.setUsuario(usuario);
    insc.setEquipo(equipo);
    insc.setNombreEquipo(equipo.getNombre());
    insc.setGrupo("A");
    sesion().persist(insc);
    return insc;
  }

  private void encuentro(Torneo torneo, InscripcionTorneo a, InscripcionTorneo b, int ga, int gb) {
    FaseTorneo fase = new FaseTorneo();
    fase.setTorneo(torneo);
    fase.setNumero(1);
    fase.setNombre("Grupos");
    sesion().persist(fase);
    EncuentroTorneo encuentro = new EncuentroTorneo();
    encuentro.setFase(fase);
    encuentro.setParticipanteA(a);
    encuentro.setParticipanteB(b);
    encuentro.setGolesA(ga);
    encuentro.setGolesB(gb);
    encuentro.setEstado("JUGADO");
    sesion().persist(encuentro);
  }

  private Equipo equipo(String nombre, Usuario owner, EstadoEquipo estado) {
    Equipo nuevo = new Equipo();
    nuevo.setNombre(nombre);
    nuevo.setFormato(5);
    nuevo.setEstado(estado);
    nuevo.setOwner(owner);
    nuevo.agregarJugador(owner);
    sesion().persist(nuevo);
    return nuevo;
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
