package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.dominio.GolPartido;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.io.UnsupportedEncodingException;
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
public class EstadisticasJugadorVistaTest {

  @Autowired
  private WebApplicationContext contexto;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;
  private Usuario usuario;

  @SuppressWarnings("null")
  @BeforeEach
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();

    usuario = new Usuario();
    usuario.setEmail("estadisticas@test.com");
    usuario.setPassword("clave-de-prueba");
    usuario.setRol("USER");
    usuario.getPerfil().setNombreUsuario("Seba");

    sessionFactory.getCurrentSession().persist(usuario);
    sessionFactory.getCurrentSession().flush();
  }

  @SuppressWarnings("null")
  @Test
  public void dadoUnUsuarioCuandoVisitaEstadisticasEntoncesVeaLasEstadisticas() throws Exception {
    mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"));
  }

  @Test
  public void dadoUnUsuarioNoLogueadoCuandoVisitaEstadisticasEntoncesRedirijaAlLogin()
    throws Exception {
    mockMvc
      .perform(get("/estadisticas"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }

  @SuppressWarnings("null")
  @Test
  public void dadoUnUsuarioAdminCuandoVisitaEstadisticasEntoncesVeaLasEstadisticas()
    throws Exception {
    usuario.setRol("ADMIN");
    sessionFactory.getCurrentSession().flush();

    mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"));
  }

  @SuppressWarnings("null")
  @Test
  public void deberiaMostrarEstadisticasEnCeroParaUnUsuarioSinPartido() throws Exception {
    mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"))
      .andExpect(
        model()
          .attribute(
            "estadisticas",
            allOf(
              hasProperty("partidosJugados", equalTo(0)),
              hasProperty("partidosGanados", equalTo(0)),
              hasProperty("partidosEmpatados", equalTo(0)),
              hasProperty("partidosPerdidos", equalTo(0)),
              hasProperty("goles", equalTo(0)),
              hasProperty("asistencias", equalTo(0)),
              hasProperty("promedioGolesPorPartido", equalTo(0.0)),
              hasProperty("promedioAsistenciasPorPartido", equalTo(0.0))
            )
          )
      );
  }

  @SuppressWarnings("null")
  @Test
  public void deberiaRenderizarLaCantidadDePartidosJugados() throws Exception {
    String html = mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"))
      .andReturn()
      .getResponse()
      .getContentAsString();

    assertThat(html, containsString("Partidos jugados: 0"));
  }

  @SuppressWarnings("null")
  @Test
  public void deberiaRenderizarLaCantidadUnoDePartidosJugados() throws Exception {
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);
    partido.registrarResultado(1, 0);

    sessionFactory.getCurrentSession().persist(partido);
    sessionFactory.getCurrentSession().flush();

    String html = mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"))
      .andReturn()
      .getResponse()
      .getContentAsString();

    assertThat(html, containsString("Partidos jugados: 1"));
  }

  @SuppressWarnings("null")
  @Test
  public void deberiaRenderizarLosGolesDelUsuarioEnElHtml() throws Exception {
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);
    partido.registrarResultado(2, 0);

    sessionFactory.getCurrentSession().persist(partido);
    sessionFactory.getCurrentSession().flush();

    GolPartido gol1 = new GolPartido();
    gol1.setPartido(partido);
    gol1.setUsuarioGoleador(usuario);
    gol1.setGoleador(usuario.getPerfil().getNombreUsuario());

    GolPartido gol2 = new GolPartido();
    gol2.setPartido(partido);
    gol2.setUsuarioGoleador(usuario);
    gol2.setGoleador(usuario.getPerfil().getNombreUsuario());

    sessionFactory.getCurrentSession().persist(gol1);
    sessionFactory.getCurrentSession().persist(gol2);
    sessionFactory.getCurrentSession().flush();

    String html = mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"))
      .andReturn()
      .getResponse()
      .getContentAsString();

    assertThat(html, containsString("Goles: 2"));
  }

  @SuppressWarnings("null")
  @Test
  public void deberiaRenderizarLosAsistenciasDelUsuarioEnElHtml() throws Exception {
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);
    partido.registrarResultado(2, 0);

    sessionFactory.getCurrentSession().persist(partido);
    sessionFactory.getCurrentSession().flush();

    Usuario goleador = new Usuario();
    goleador.setEmail("goleador@test.com");
    goleador.setPassword("clave-de-prueba");
    goleador.setRol("USER");
    goleador.getPerfil().setNombreUsuario("Sebastian");
    sessionFactory.getCurrentSession().persist(goleador);

    GolPartido gol1 = new GolPartido();
    gol1.setPartido(partido);
    gol1.setUsuarioGoleador(goleador);
    gol1.setGoleador(goleador.getPerfil().getNombreUsuario());
    gol1.setUsuarioAsistente(usuario);
    gol1.setAsistidor(usuario.getPerfil().getNombreUsuario());

    GolPartido gol2 = new GolPartido();
    gol2.setPartido(partido);
    gol2.setUsuarioGoleador(goleador);
    gol2.setGoleador(goleador.getPerfil().getNombreUsuario());
    gol2.setUsuarioAsistente(usuario);
    gol2.setAsistidor(usuario.getPerfil().getNombreUsuario());

    sessionFactory.getCurrentSession().persist(gol1);
    sessionFactory.getCurrentSession().persist(gol2);
    sessionFactory.getCurrentSession().flush();

    String html = mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"))
      .andReturn()
      .getResponse()
      .getContentAsString();

    assertThat(html, containsString("Asistencias: 2"));
  }

  @SuppressWarnings("null")
  @Test
  public void deberiaRenderizarLosPromediosDeGolesEnDosDecimales()
    throws UnsupportedEncodingException, Exception {
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);
    partido.registrarResultado(1, 0);
    sessionFactory.getCurrentSession().persist(partido);
    sessionFactory.getCurrentSession().flush();

    Partido partido2 = new Partido();
    partido2.getEquipoA().add(usuario);
    partido2.registrarResultado(0, 0);
    sessionFactory.getCurrentSession().persist(partido2);
    sessionFactory.getCurrentSession().flush();

    GolPartido gol1 = new GolPartido();
    gol1.setPartido(partido);
    gol1.setUsuarioGoleador(usuario);
    gol1.setGoleador(usuario.getPerfil().getNombreUsuario());

    sessionFactory.getCurrentSession().persist(gol1);
    sessionFactory.getCurrentSession().flush();

    String html = mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"))
      .andReturn()
      .getResponse()
      .getContentAsString();

    assertThat(html, containsString("Promedio de Goles: 0,50"));
  }

  @SuppressWarnings("null")
  @Test
  public void deberiaRenderizarLosPromediosDeAsistenciasEnDosDecimales()
    throws UnsupportedEncodingException, Exception {
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);
    partido.registrarResultado(2, 0);

    Partido partido2 = new Partido();
    partido2.getEquipoA().add(usuario);
    partido2.registrarResultado(0, 0);

    sessionFactory.getCurrentSession().persist(partido2);
    sessionFactory.getCurrentSession().flush();

    sessionFactory.getCurrentSession().persist(partido);
    sessionFactory.getCurrentSession().flush();

    Usuario goleador = new Usuario();
    goleador.setEmail("goleador@test.com");
    goleador.setPassword("clave-de-prueba");
    goleador.setRol("USER");
    goleador.getPerfil().setNombreUsuario("Sebastian");
    sessionFactory.getCurrentSession().persist(goleador);

    GolPartido gol1 = new GolPartido();
    gol1.setPartido(partido);
    gol1.setUsuarioGoleador(goleador);
    gol1.setGoleador(goleador.getPerfil().getNombreUsuario());
    gol1.setUsuarioAsistente(usuario);
    gol1.setAsistidor(usuario.getPerfil().getNombreUsuario());

    sessionFactory.getCurrentSession().persist(gol1);
    sessionFactory.getCurrentSession().flush();

    String html = mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"))
      .andReturn()
      .getResponse()
      .getContentAsString();

    assertThat(html, containsString("Promedio de Asistencias: 0,50"));
  }

  @SuppressWarnings("null")
  @Test
  public void deberiaRenderizarLosPromediosEnCeroCuandoNoHayPartidos()
    throws UnsupportedEncodingException, Exception {
    Partido partido = new Partido();
    partido.getEquipoA().add(usuario);
    partido.registrarResultado(0, 0);

    Partido partido2 = new Partido();
    partido2.getEquipoA().add(usuario);
    partido2.registrarResultado(0, 0);

    sessionFactory.getCurrentSession().persist(partido2);
    sessionFactory.getCurrentSession().flush();

    sessionFactory.getCurrentSession().persist(partido);
    sessionFactory.getCurrentSession().flush();

    Usuario goleador = new Usuario();
    goleador.setEmail("goleador@test.com");
    goleador.setPassword("clave-de-prueba");
    goleador.setRol("USER");
    goleador.getPerfil().setNombreUsuario("Sebastian");
    sessionFactory.getCurrentSession().persist(goleador);

    GolPartido gol1 = new GolPartido();
    gol1.setPartido(partido);
    gol1.setUsuarioGoleador(goleador);
    gol1.setGoleador(goleador.getPerfil().getNombreUsuario());
    gol1.setUsuarioAsistente(usuario);
    gol1.setAsistidor(usuario.getPerfil().getNombreUsuario());

    sessionFactory.getCurrentSession().persist(gol1);
    sessionFactory.getCurrentSession().flush();

    String html = mockMvc
      .perform(get("/estadisticas").sessionAttr("usuario", usuario))
      .andExpect(status().isOk())
      .andExpect(view().name("estadisticas"))
      .andReturn()
      .getResponse()
      .getContentAsString();

    assertThat(html, containsString("Promedio de Goles: 0,00"));
  }
}
