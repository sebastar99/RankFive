package com.tallerwebi.integracion;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadoEquipo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
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
public class RankingEquiposVistaTest {

  @Autowired
  private WebApplicationContext contexto;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;
  private Usuario usuarioSesion;

  @BeforeEach
  public void preparar() {
    mockMvc = MockMvcBuilders.webAppContextSetup(requireNonNull(contexto)).build();
    usuarioSesion = usuario("miUsuario", 1000);
    sesion().flush();
  }

  @Test
  public void muestraSoloElTop10RealPorPlPromedioYNoLosEquiposDelMockup() throws Exception {
    for (int i = 1; i <= 12; i++) {
      equipo("Real " + i, usuario("jug" + i, 2000 + i * 10));
    }

    String html = html();

    assertThat(html, containsString("Real 12"));
    assertThat(html, containsString("Real 3"));
    assertThat(html, not(containsString("Real 2<")));
    assertThat(html, not(containsString("Real 1<")));
    assertThat(html, not(containsString("Deportivo Sur")));
    assertThat(html, not(containsString("Dream Team")));
    assertEquals(10, ocurrencias(html, "team-rank-row"));
  }

  @Test
  public void fijaLosEquiposDelUsuarioFueraDelTopConSuPosicionReal() throws Exception {
    for (int i = 1; i <= 11; i++) {
      equipo("Real " + i, usuario("jug" + i, 3000 + i));
    }
    equipo("Mi Equipo Bajo", usuarioSesion);

    String html = html();

    assertThat(html, containsString("Mi Equipo Bajo"));
    assertThat(html, containsString("team-rank-row mine"));
    assertThat(html, containsString(">12<"));
    assertThat(html, containsString("Tu equipo"));
    assertEquals(11, ocurrencias(html, "team-rank-row"));
  }

  @Test
  public void noDuplicaUnEquipoPropioQueYaEstaEnElTop() throws Exception {
    equipo("Mi Equipo Top", usuarioSesion);
    equipo("Otro", usuario("otro", 500));

    String html = html();

    assertEquals(1, ocurrencias(html, "Mi Equipo Top"));
    assertEquals(2, ocurrencias(html, "team-rank-row"));
  }

  @Test
  public void ignoraEquiposPendientes() throws Exception {
    Equipo pendiente = equipo("Pendiente FC", usuario("pend", 3500));
    pendiente.setEstado(EstadoEquipo.PENDIENTE);
    sesion().flush();

    assertThat(html(), not(containsString("Pendiente FC")));
  }

  private String html() throws Exception {
    sesion().flush();
    sesion().clear();
    return mockMvc
      .perform(
        get("/ranking-equipos")
          .sessionAttr("usuario", java.util.Objects.requireNonNull(usuarioSesion))
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private static int ocurrencias(String texto, String patron) {
    int cantidad = 0;
    int indice = texto.indexOf(patron);
    while (indice >= 0) {
      cantidad++;
      indice = texto.indexOf(patron, indice + patron.length());
    }
    return cantidad;
  }

  private Equipo equipo(String nombre, Usuario owner) {
    Equipo nuevo = new Equipo();
    nuevo.setNombre(nombre);
    nuevo.setFormato(5);
    nuevo.setEstado(EstadoEquipo.ACTIVO);
    nuevo.setOwner(owner);
    nuevo.agregarJugador(owner);
    sesion().persist(nuevo);
    return nuevo;
  }

  private Usuario usuario(String nombre, int pl) {
    Usuario usuario = new Usuario();
    usuario.setEmail(nombre + "@test.com");
    usuario.setPassword("123");
    usuario.setRol("USER");
    usuario.getPerfil().setNombreUsuario(nombre);
    usuario.getPerfil().setPl(pl);
    sesion().persist(usuario);
    return usuario;
  }

  private Session sesion() {
    return sessionFactory.getCurrentSession();
  }
}
