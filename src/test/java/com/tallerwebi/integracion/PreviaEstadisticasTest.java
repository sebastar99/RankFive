package com.tallerwebi.integracion;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.enums.TipoTarjeta;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.time.LocalDateTime;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
public class PreviaEstadisticasTest {

  @Autowired
  private WebApplicationContext contexto;

  @Autowired
  private SessionFactory sessionFactory;

  @MockitoBean
  private ServicioGemini gemini;

  private MockMvc mockMvc;

  @BeforeEach
  void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(requireNonNull(contexto)).build();
  }

  @Test
  void previaConsultaElHistorialPersistidoDeAmbosEquiposSinContarPartidosPendientes()
    throws Exception {
    Usuario seba = guardarUsuario("Seba");
    Usuario ana = guardarUsuario("Ana");
    Partido jugado = new Partido();
    jugado.getEquipoA().add(seba);
    jugado.getEquipoA().add(ana);
    jugado.registrarResultado(2, 0);
    sessionFactory.getCurrentSession().persist(jugado);
    guardarGol(jugado, seba, ana);
    guardarGol(jugado, seba, ana);
    guardarTarjeta(jugado, seba, TipoTarjeta.AMARILLA);
    guardarTarjeta(jugado, ana, TipoTarjeta.ROJA);

    Partido pendiente = new Partido();
    pendiente.setFecha(LocalDateTime.of(2026, 10, 20, 20, 0));
    pendiente.getEquipoA().add(seba);
    pendiente.getEquipoB().add(ana);
    sessionFactory.getCurrentSession().persist(pendiente);
    guardarGol(pendiente, seba, ana);
    guardarTarjeta(pendiente, seba, TipoTarjeta.ROJA);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
    when(gemini.generarTexto(anyString())).thenReturn("Análisis basado en estadísticas");

    mockMvc
      .perform(
        post("/api/ia/partido/{id}/previa", pendiente.getId())
          .sessionAttr("usuario", requireNonNull(seba))
      )
      .andExpect(status().isOk())
      .andExpect(content().string("Análisis basado en estadísticas"));

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(gemini).generarTexto(captor.capture());
    String prompt = captor.getValue();
    String equipoA = prompt.substring(
      prompt.indexOf("\nEQUIPO A\n"),
      prompt.indexOf("\nEQUIPO B\n")
    );
    String equipoB = prompt.substring(
      prompt.indexOf("\nEQUIPO B\n"),
      prompt.indexOf("FORMATO DE RESPUESTA")
    );
    assertAll(
      () -> assertTrue(equipoA.contains("Seba")),
      () -> assertTrue(equipoA.contains("Partidos jugados: 1")),
      () -> assertTrue(equipoA.contains("Goles: 2")),
      () -> assertTrue(equipoA.contains("Asistencias: 0")),
      () -> assertTrue(equipoA.contains("Amarillas: 1; Rojas: 0")),
      () -> assertTrue(equipoB.contains("Ana")),
      () -> assertTrue(equipoB.contains("Partidos jugados: 1")),
      () -> assertTrue(equipoB.contains("Goles: 0")),
      () -> assertTrue(equipoB.contains("Asistencias: 2")),
      () -> assertTrue(equipoB.contains("Amarillas: 0; Rojas: 1"))
    );
  }

  @Test
  void sinSesionNoGeneraPrevia() throws Exception {
    mockMvc.perform(post("/api/ia/partido/10/previa")).andExpect(status().isForbidden());
    verifyNoInteractions(gemini);
  }

  @Test
  void usuarioAjenoAlPartidoNoGeneraPrevia() throws Exception {
    Usuario ajeno = guardarUsuario("Ajeno");
    Partido partido = new Partido();
    partido.getEquipoA().add(guardarUsuario("Participante"));
    sessionFactory.getCurrentSession().persist(partido);
    sessionFactory.getCurrentSession().flush();
    mockMvc
      .perform(
        post("/api/ia/partido/{id}/previa", partido.getId())
          .sessionAttr("usuario", requireNonNull(ajeno))
      )
      .andExpect(status().isForbidden());
    verifyNoInteractions(gemini);
  }

  private Usuario guardarUsuario(String nombre) {
    Usuario usuario = new Usuario();
    usuario.setEmail(nombre + "@test.com");
    usuario.setPassword("clave-test");
    usuario.setRol("USER");
    usuario.getPerfil().setNombreCompleto(nombre);
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private void guardarGol(Partido partido, Usuario goleador, Usuario asistente) {
    GolPartido gol = new GolPartido();
    gol.setPartido(partido);
    gol.setUsuarioGoleador(goleador);
    gol.setUsuarioAsistente(asistente);
    gol.setGoleador(goleador.getPerfil().getNombreCompleto());
    gol.setAsistidor(asistente.getPerfil().getNombreCompleto());
    sessionFactory.getCurrentSession().persist(gol);
  }

  private void guardarTarjeta(Partido partido, Usuario jugador, TipoTarjeta tipo) {
    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(jugador);
    tarjeta.setJugador(jugador.getPerfil().getNombreCompleto());
    tarjeta.setTipo(tipo);
    sessionFactory.getCurrentSession().persist(tarjeta);
  }
}
