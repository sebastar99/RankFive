package com.tallerwebi.integracion;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadoEquipo;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
@Transactional
public class DashboardCapitanVistaTest {

  @Autowired
  private WebApplicationContext contexto;

  @Autowired
  private SessionFactory sessionFactory;

  @MockitoBean
  private ServicioEquipo servicioEquipo;

  @Test
  public void marcaAlCreadorComoCapitanAunqueEsteSegundoEnElRanking() throws Exception {
    Usuario creador = new Usuario();
    creador.setEmail("capitan@test.com");
    creador.setPassword("clave");
    creador.setRol("USER");
    creador.getPerfil().setNombreUsuario("capitan");
    creador.getPerfil().setNombreCompleto("CapitanNombre");
    creador.getPerfil().setPl(1000);
    sessionFactory.getCurrentSession().persist(creador);
    sessionFactory.getCurrentSession().flush();

    Usuario companero = new Usuario();
    companero.setId(creador.getId() + 1);
    companero.setEmail("companero@test.com");
    companero.getPerfil().setNombreCompleto("CompaneroNombre");
    companero.getPerfil().setPl(2000);

    Equipo equipo = new Equipo();
    equipo.setId(5L);
    equipo.setNombre("Los Pibes");
    equipo.setFormato(5);
    equipo.setOwner(creador);
    equipo.setEstado(EstadoEquipo.ACTIVO);
    equipo.agregarJugador(creador);
    equipo.agregarJugador(companero);
    when(servicioEquipo.listarEquiposActivosDe(creador)).thenReturn(List.of(equipo));

    String html = MockMvcBuilders
      .webAppContextSetup(requireNonNull(contexto))
      .build()
      .perform(get("/dashboard").sessionAttr("usuario", requireNonNull(creador)))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
    String ranking = html.substring(html.indexOf("team-card\""));
    int indiceCompanero = ranking.indexOf("CompaneroNombre");
    int indiceCapitan = ranking.indexOf("CapitanNombre");
    String filaCompanero = ranking.substring(
      indiceCompanero,
      ranking.indexOf("</div>", indiceCompanero)
    );
    String filaCapitan = ranking.substring(indiceCapitan, ranking.indexOf("</div>", indiceCapitan));

    assertTrue(indiceCompanero < indiceCapitan);
    assertFalse(filaCompanero.contains("class=\"captain-mark\""));
    assertTrue(filaCapitan.contains("class=\"captain-mark\""));
    assertTrue(filaCapitan.contains("title=\"Capitán\""));
    assertTrue(filaCapitan.contains(">C</span>"));
    assertEquals(1, html.split("class=\"captain-mark\"", -1).length - 1);
  }
}
