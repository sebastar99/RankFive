package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorAccesoAdminTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  private Usuario noAdmin;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    noAdmin = new Usuario();
    noAdmin.setRol("USER");
  }

  @Test
  public void noAdmin_noPuedeVerFormNuevoTorneo() throws Exception {
    MvcResult res =
      this.mockMvc.perform(get("/torneos/nuevo").sessionAttr("usuario", noAdmin))
        .andExpect(status().is3xxRedirection())
        .andReturn();
    ModelAndView mav = res.getModelAndView();
    assert mav != null;
    assertThat(Objects.requireNonNull(mav.getViewName()), equalTo("redirect:/torneos"));
  }

  @Test
  public void noAdmin_noPuedeVerFormNuevaLiga() throws Exception {
    MvcResult res =
      this.mockMvc.perform(get("/ligas/nueva").sessionAttr("usuario", noAdmin))
        .andExpect(status().is3xxRedirection())
        .andReturn();
    ModelAndView mav = res.getModelAndView();
    assert mav != null;
    assertThat(Objects.requireNonNull(mav.getViewName()), equalTo("redirect:/torneos"));
  }

  @Test
  public void noAdmin_noPuedeGenerarFixtureTorneo() throws Exception {
    MvcResult res =
      this.mockMvc.perform(
          post("/torneos/generar-fixture").param("id", "1").sessionAttr("usuario", noAdmin)
        )
        .andExpect(status().is3xxRedirection())
        .andReturn();
    ModelAndView mav = res.getModelAndView();
    assert mav != null;
    assertThat(
      Objects.requireNonNull(mav.getViewName()),
      equalTo("redirect:/torneos/detalle?id=1&aviso=FIXTURE_INVALIDO")
    );
  }

  @Test
  public void noAdmin_noPuedeGenerarFixtureLiga() throws Exception {
    MvcResult res =
      this.mockMvc.perform(
          post("/ligas/generar-fixture").param("id", "1").sessionAttr("usuario", noAdmin)
        )
        .andExpect(status().is3xxRedirection())
        .andReturn();
    ModelAndView mav = res.getModelAndView();
    assert mav != null;
    assertThat(
      Objects.requireNonNull(mav.getViewName()),
      equalTo("redirect:/ligas/detalle?id=1&aviso=FIXTURE_INVALIDO")
    );
  }

  @Test
  public void noAdmin_noPuedeRegistrarResultadoTorneo() throws Exception {
    MvcResult res =
      this.mockMvc.perform(
          post("/torneos/encuentro/resultado")
            .param("torneoId", "1")
            .param("encuentroId", "1")
            .param("golesA", "1")
            .param("golesB", "0")
            .sessionAttr("usuario", noAdmin)
        )
        .andExpect(status().is3xxRedirection())
        .andReturn();
    ModelAndView mav = res.getModelAndView();
    assert mav != null;
    assertThat(
      Objects.requireNonNull(mav.getViewName()),
      equalTo("redirect:/torneos/detalle?id=1&aviso=RESULTADO_INVALIDO")
    );
  }

  @Test
  public void noAdmin_noPuedeRegistrarResultadoLiga() throws Exception {
    MvcResult res =
      this.mockMvc.perform(
          post("/ligas/partido/resultado")
            .param("ligaId", "1")
            .param("partidoId", "1")
            .param("golesLocal", "1")
            .param("golesVisitante", "0")
            .sessionAttr("usuario", noAdmin)
        )
        .andExpect(status().is3xxRedirection())
        .andReturn();
    ModelAndView mav = res.getModelAndView();
    assert mav != null;
    assertThat(
      Objects.requireNonNull(mav.getViewName()),
      equalTo("redirect:/ligas/detalle?id=1&aviso=RESULTADO_INVALIDO")
    );
  }
}
