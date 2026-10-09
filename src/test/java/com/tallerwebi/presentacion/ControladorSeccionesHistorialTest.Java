package com.tallerwebi.presentacion;
 
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
 
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.ServicioArbitro;
import com.tallerwebi.dominio.ServicioCompetencia;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.ServicioPartido;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
 
public class ControladorSeccionesHistorialTest {
 
  private ServicioRelacionAmistad servicioRelacionAmistad;
  private ServicioPartido servicioPartido;
  private ServicioCompetencia servicioCompetencia;
  private ServicioArbitro servicioArbitro;
  private ServicioLiga servicioLiga;
  private ControladorSecciones controlador;
 
  @BeforeEach
  public void setup() {
    servicioRelacionAmistad = mock(ServicioRelacionAmistad.class);
    servicioPartido = mock(ServicioPartido.class);
    servicioCompetencia = mock(ServicioCompetencia.class);
    servicioArbitro = mock(ServicioArbitro.class);
    servicioLiga = mock(ServicioLiga.class);
    controlador =
      new ControladorSecciones(
        servicioRelacionAmistad,
        servicioPartido,
        servicioCompetencia,
        servicioArbitro,
        servicioLiga
      );
    when(servicioRelacionAmistad.listarSolicitudesPendientes(any()))
      .thenReturn(java.util.List.of());
    when(servicioRelacionAmistad.contarSolicitudesPendientes(any())).thenReturn((long) 0);
  }
 
  @Test
  public void usuarioEnEquipoAGana() {
    Usuario usuarioSesion = new Usuario();
    usuarioSesion.setId(1L);
    Usuario usuarioEnPartido = new Usuario();
    usuarioEnPartido.setId(1L);
 
    Usuario usuarioRival = new Usuario();
    usuarioRival.setId(2L);
 
    Set<Usuario> equipoA = new java.util.HashSet<>();
    equipoA.add(usuarioEnPartido);
 
    Set<Usuario> equipoB = new java.util.HashSet<>();
    equipoB.add(usuarioRival);
 
    Partido partido = new Partido();
    when(servicioPartido.listarPartidosDe(eq(usuarioSesion))).thenReturn(List.of(partido));
    partido.setEquipoA(equipoA);
    partido.setEquipoB(equipoB);
    partido.registrarResultado(5, 0);
 
    MockHttpServletRequest req = new MockHttpServletRequest();
    HttpSession ses = req.getSession(true);
    ses.setAttribute(ControladorBase.ATRIBUTO_USUARIO, usuarioSesion);
 
    ModelAndView mv = controlador.historial(req);
    var resultadoPartido = mv.getModel();
 
    assertEquals(1, resultadoPartido.get("jugados"));
    assertEquals(1, resultadoPartido.get("ganados"));
    assertEquals(0, resultadoPartido.get("empatados"));
    assertEquals(0, resultadoPartido.get("perdidos"));
    assertEquals(100, resultadoPartido.get("efectividad"));
  }
 
  @Test
  public void usuarioEnEquipoAPierde() {
    Usuario usuarioSesion = new Usuario();
    usuarioSesion.setId(1L);
    Usuario usuarioEnPartido = new Usuario();
    usuarioEnPartido.setId(1L);
 
    Usuario usuarioRival = new Usuario();
    usuarioRival.setId(2L);
 
    Set<Usuario> equipoA = new java.util.HashSet<>();
    equipoA.add(usuarioEnPartido);
 
    Set<Usuario> equipoB = new java.util.HashSet<>();
    equipoB.add(usuarioRival);
 
    Partido partido = new Partido();
    when(servicioPartido.listarPartidosDe(eq(usuarioSesion))).thenReturn(List.of(partido));
    partido.setEquipoA(equipoA);
    partido.setEquipoB(equipoB);
    partido.registrarResultado(5, 6);
 
    MockHttpServletRequest req = new MockHttpServletRequest();
    HttpSession ses = req.getSession(true);
    ses.setAttribute(ControladorBase.ATRIBUTO_USUARIO, usuarioSesion);
 
    ModelAndView mv = controlador.historial(req);
    var resultadoPartido = mv.getModel();
 
    assertEquals(1, resultadoPartido.get("jugados"));
    assertEquals(0, resultadoPartido.get("ganados"));
    assertEquals(0, resultadoPartido.get("empatados"));
    assertEquals(1, resultadoPartido.get("perdidos"));
    assertEquals(0, resultadoPartido.get("efectividad"));
  }
 
  @Test
  public void usuarioEnEquipoEmpata() {
    Usuario usuarioSesion = new Usuario();
    usuarioSesion.setId(1L);
    Usuario usuarioEnPartido = new Usuario();
    usuarioEnPartido.setId(1L);
 
    Usuario usuarioRival = new Usuario();
    usuarioRival.setId(2L);
 
    Set<Usuario> equipoA = new java.util.HashSet<>();
    equipoA.add(usuarioEnPartido);
 
    Set<Usuario> equipoB = new java.util.HashSet<>();
    equipoB.add(usuarioRival);
 
    Partido partido = new Partido();
    when(servicioPartido.listarPartidosDe(eq(usuarioSesion))).thenReturn(List.of(partido));
    partido.setEquipoA(equipoA);
    partido.setEquipoB(equipoB);
    partido.registrarResultado(5, 5);
 
    MockHttpServletRequest req = new MockHttpServletRequest();
    HttpSession ses = req.getSession(true);
    ses.setAttribute(ControladorBase.ATRIBUTO_USUARIO, usuarioSesion);
 
    ModelAndView mv = controlador.historial(req);
    var resultadoPartido = mv.getModel();
 
    assertEquals(1, resultadoPartido.get("jugados"));
    assertEquals(0, resultadoPartido.get("ganados"));
    assertEquals(1, resultadoPartido.get("empatados"));
    assertEquals(0, resultadoPartido.get("perdidos"));
    assertEquals(50, resultadoPartido.get("efectividad"));
  }
 
  @Test
  public void usuarioEnEquipoBGana() {
    Usuario usuarioSesion = new Usuario();
    usuarioSesion.setId(1L);
    Usuario usuarioEnPartido = new Usuario();
    usuarioEnPartido.setId(1L);
 
    Usuario usuarioRival = new Usuario();
    usuarioRival.setId(2L);
 
    Set<Usuario> equipoB = new java.util.HashSet<>();
    equipoB.add(usuarioEnPartido);
 
    Set<Usuario> equipoA = new java.util.HashSet<>();
    equipoA.add(usuarioRival);
 
    Partido partido = new Partido();
    when(servicioPartido.listarPartidosDe(eq(usuarioSesion))).thenReturn(List.of(partido));
    partido.setEquipoA(equipoA);
    partido.setEquipoB(equipoB);
    partido.registrarResultado(0, 5);
 
    MockHttpServletRequest req = new MockHttpServletRequest();
    HttpSession ses = req.getSession(true);
    ses.setAttribute(ControladorBase.ATRIBUTO_USUARIO, usuarioSesion);
 
    ModelAndView mv = controlador.historial(req);
    var resultadoPartido = mv.getModel();
 
    assertEquals(1, resultadoPartido.get("jugados"));
    assertEquals(1, resultadoPartido.get("ganados"));
    assertEquals(0, resultadoPartido.get("empatados"));
    assertEquals(0, resultadoPartido.get("perdidos"));
    assertEquals(100, resultadoPartido.get("efectividad"));
  }
 
  @Test
  public void usuarioEnEquipoBPierde() {
    Usuario usuarioSesion = new Usuario();
    usuarioSesion.setId(1L);
    Usuario usuarioEnPartido = new Usuario();
    usuarioEnPartido.setId(1L);
 
    Usuario usuarioRival = new Usuario();
    usuarioRival.setId(2L);
 
    Set<Usuario> equipoB = new java.util.HashSet<>();
    equipoB.add(usuarioEnPartido);
 
    Set<Usuario> equipoA = new java.util.HashSet<>();
    equipoA.add(usuarioRival);
 
    Partido partido = new Partido();
    when(servicioPartido.listarPartidosDe(eq(usuarioSesion))).thenReturn(List.of(partido));
    partido.setEquipoA(equipoA);
    partido.setEquipoB(equipoB);
    partido.registrarResultado(5, 0);
 
    MockHttpServletRequest req = new MockHttpServletRequest();
    HttpSession ses = req.getSession(true);
    ses.setAttribute(ControladorBase.ATRIBUTO_USUARIO, usuarioSesion);
 
    ModelAndView mv = controlador.historial(req);
    var resultadoPartido = mv.getModel();
 
    assertEquals(1, resultadoPartido.get("jugados"));
    assertEquals(0, resultadoPartido.get("ganados"));
    assertEquals(0, resultadoPartido.get("empatados"));
    assertEquals(1, resultadoPartido.get("perdidos"));
    assertEquals(0, resultadoPartido.get("efectividad"));
  }
}
