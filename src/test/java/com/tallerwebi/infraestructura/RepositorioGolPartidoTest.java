package com.tallerwebi.infraestructura;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.GolPartido;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioGolPartido;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HibernateInfraestructuraTestConfig.class)
public class RepositorioGolPartidoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioGolPartido repositorio;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioGolPartidoImpl(sessionFactory);
  }

  private Usuario guardarUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("1234");
    usuario.setRol("USER");
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Partido guardarPartido(Usuario jugador) {
    Partido partido = new Partido();
    partido.getEquipoA().add(jugador);
    partido.registrarResultado(2, 0);
    sessionFactory.getCurrentSession().persist(partido);
    return partido;
  }

  private org.hibernate.Session sesion() {
    return sessionFactory.getCurrentSession();
  }

  @Test
  @Transactional
  public void deberiaGuardarYRecuperarUnGolConSuUsuarioGoleadorYAsistente() {
    Usuario goleador = guardarUsuario("goleador@test.com");
    Usuario asistente = guardarUsuario("asistente@test.com");
    Partido partido = guardarPartido(goleador);

    GolPartido gol = new GolPartido();
    gol.setPartido(partido);
    gol.setGoleador("goleador@test.com");
    gol.setAsistidor("asistente@test.com");
    gol.setUsuarioGoleador(goleador);
    gol.setUsuarioAsistente(asistente);
    repositorio.guardar(gol);

    sesion().flush();
    sesion().clear();

    // Acción
    List<GolPartido> goles = repositorio.listarPorPartido(partido.getId());

    // Verificación
    assertEquals(1, goles.size());
    GolPartido recuperado = goles.get(0);
    assertEquals(goleador.getId(), recuperado.getUsuarioGoleador().getId());
    assertEquals(asistente.getId(), recuperado.getUsuarioAsistente().getId());
  }
}
