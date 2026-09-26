package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.ResultadoInscripcion;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLigaTest {

  private ServicioLiga servicioLiga;
  private ControladorLiga controlador;
  private HttpServletRequest request;
  private Usuario usuario;

  @BeforeEach
  public void init() {
    servicioLiga = mock(ServicioLiga.class);
    controlador =
      new ControladorLiga(
        mock(ServicioRelacionAmistad.class),
        servicioLiga,
        mock(ServicioCompetencia.class)
      );
    request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    usuario = new Usuario();
    when(request.getSession(false)).thenReturn(session);
    when(session.getAttribute("usuario")).thenReturn(usuario);
  }

  @Test
  public void detalleMuestraLigaConAviso() {
    Liga liga = new Liga();
    liga.setId(4L);
    when(servicioLiga.buscarPorId(4L)).thenReturn(liga);

    ModelAndView mav = controlador.detalle(request, 4L, Avisos.FIXTURE_GENERADO);

    assertThat(mav.getViewName(), equalTo("ligaDetalle"));
    assertThat(mav.getModel().get("liga"), equalTo(liga));
    assertThat(mav.getModel().get("avisoOk"), equalTo(true));
  }

  @Test
  public void detalleDeLigaInexistenteRedirige() {
    assertThat(controlador.detalle(request, 9L, null).getViewName(), equalTo("redirect:/torneos"));
  }

  @Test
  public void inscripcionRedirigeSegunResultado() {
    when(servicioLiga.inscribir(usuario, 4L)).thenReturn(ResultadoInscripcion.INSCRIPTO);
    assertThat(
      controlador.inscribirse(request, 4L).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=INSCRIPTO")
    );

    when(servicioLiga.inscribir(usuario, 4L)).thenReturn(ResultadoInscripcion.SIN_EQUIPO_ACTIVO);
    assertThat(
      controlador.inscribirse(request, 4L).getViewName(),
      equalTo("redirect:/torneos?aviso=SIN_EQUIPO_ACTIVO")
    );
  }

  @Test
  public void generarFixtureYResultadoInformanElResultado() {
    when(servicioLiga.generarFixtureSiNoExiste(4L)).thenReturn(true);
    assertThat(
      controlador.generarFixture(request, 4L).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=FIXTURE_GENERADO")
    );

    when(servicioLiga.registrarResultado(7L, 1, 0)).thenReturn(false);
    assertThat(
      controlador.registrarResultado(request, 4L, 7L, 1, 0).getViewName(),
      equalTo("redirect:/ligas/detalle?id=4&aviso=RESULTADO_INVALIDO")
    );
  }
}
