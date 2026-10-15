package com.tallerwebi.integracion;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.dominio.ResultadoCreacionEquipo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
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
public class EquipoCreacionVistaTest {

  @Autowired
  private WebApplicationContext contexto;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;
  private Usuario usuario;

  @BeforeEach
  public void preparar() {
    mockMvc = MockMvcBuilders.webAppContextSetup(requireNonNull(contexto)).build();
    usuario = new Usuario();
    usuario.setEmail("creador@test.com");
    usuario.setPassword("clave");
    usuario.setRol("USER");
    usuario.getPerfil().setNombreUsuario("creador");
    sessionFactory.getCurrentSession().persist(usuario);
    sessionFactory.getCurrentSession().flush();
  }

  @Test
  public void elFormularioExigeMinimoSinLimitarSuplentes() throws Exception {
    String html = mockMvc
      .perform(get("/agregar-equipo").sessionAttr("usuario", requireNonNull(usuario)))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    assertThat(html, containsString("invitá al menos"));
    assertThat(html, containsString("id=\"minJugadores\">4</span>"));
    assertThat(html, containsString("id=\"crearEquipoBtn\""));
    assertThat(html, not(containsString("maxSeleccion()")));
    assertThat(html, not(containsString("b.checked = false")));
  }

  @Test
  public void elServidorRechazaUnPostConMenosJugadoresAunqueSeOmitaElJavascript() throws Exception {
    mockMvc
      .perform(
        post("/agregar-equipo")
          .sessionAttr("usuario", requireNonNull(usuario))
          .param("nombre", "Equipo incompleto")
          .param("cantidad", "5")
          .param("jugadores", "amigo1", "amigo2", "amigo3")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("agregarEquipo"))
      .andExpect(
        model().attribute("error", ResultadoCreacionEquipo.JUGADORES_INSUFICIENTES.getMensaje())
      );
  }
}
