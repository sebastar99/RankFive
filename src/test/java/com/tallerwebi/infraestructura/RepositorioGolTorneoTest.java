package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.EstadisticasGoles;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.GolTorneo;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.RepositorioGolTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.Session;
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
@Transactional
public class RepositorioGolTorneoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioGolTorneo repositorio;
  private Torneo torneo;
  private InscripcionTorneo alfa;
  private InscripcionTorneo beta;
  private EncuentroTorneo encuentro;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioGolTorneoImpl(sessionFactory);
    torneo = torneo("Copa A");
    alfa = inscripcion(torneo, "alfa@mail.com", "Alfa");
    beta = inscripcion(torneo, "beta@mail.com", "Beta");
    encuentro = encuentro(torneo, alfa, beta);
  }

  @Test
  public void guardarListarYContarGolesDelTorneo() {
    repositorio.guardar(gol(encuentro, alfa, "Messi", "Di María"));
    repositorio.guardar(gol(encuentro, alfa, "Messi", null));
    repositorio.guardar(gol(encuentro, beta, "Alvarez", null));
    sesion().flush();
    sesion().clear();

    List<GolTorneo> goles = repositorio.listarPorTorneo(torneo.getId());

    assertThat(goles, hasSize(3));
    assertThat(goles.get(0).getEquipoNombre(), equalTo("Alfa"));
    assertThat(goles.get(0).getAsistidor(), equalTo("Di María"));
    assertThat(goles.get(0).getPartidoId(), equalTo(encuentro.getId()));
    assertThat(repositorio.contarPor(encuentro.getId(), alfa.getId()), equalTo(2L));
    assertThat(repositorio.contarPor(encuentro.getId(), beta.getId()), equalTo(1L));
    assertThat(repositorio.contarPor(encuentro.getId(), 999L), equalTo(0L));
  }

  @Test
  public void listarNoIncluyeGolesDeOtrosTorneos() {
    Torneo otro = torneo("Copa B");
    InscripcionTorneo x = inscripcion(otro, "x@mail.com", "X");
    InscripcionTorneo y = inscripcion(otro, "y@mail.com", "Y");
    repositorio.guardar(gol(encuentro(otro, x, y), x, "Otro", null));
    repositorio.guardar(gol(encuentro, beta, "Alvarez", null));
    sesion().flush();

    List<GolTorneo> goles = repositorio.listarPorTorneo(torneo.getId());

    assertThat(goles, hasSize(1));
    assertThat(goles.get(0).getGoleador(), equalTo("Alvarez"));
    assertThat(repositorio.listarPorTorneo(999L), empty());
  }

  private Session sesion() {
    return sessionFactory.getCurrentSession();
  }

  private Torneo torneo(String nombre) {
    Torneo t = new Torneo();
    t.setNombre(nombre);
    t.setFormato(5);
    t.setCupoEquipos(16);
    t.setInscripcionAbierta(true);
    t.setFechaInicio(LocalDate.now());
    t.setUbicacion("CABA");
    sesion().persist(t);
    return t;
  }

  private InscripcionTorneo inscripcion(Torneo t, String email, String nombre) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("123");
    sesion().persist(usuario);
    InscripcionTorneo insc = new InscripcionTorneo();
    insc.setTorneo(t);
    insc.setUsuario(usuario);
    insc.setNombreEquipo(nombre);
    sesion().persist(insc);
    return insc;
  }

  private EncuentroTorneo encuentro(Torneo t, InscripcionTorneo a, InscripcionTorneo b) {
    FaseTorneo fase = new FaseTorneo();
    fase.setNumero(1);
    fase.setNombre("Final");
    fase.setTorneo(t);
    sesion().persist(fase);
    EncuentroTorneo e = new EncuentroTorneo();
    e.setFase(fase);
    e.setParticipanteA(a);
    e.setParticipanteB(b);
    e.setGolesA(2);
    e.setGolesB(1);
    e.setEstado("JUGADO");
    sesion().persist(e);
    return e;
  }

  private static GolTorneo gol(
    EncuentroTorneo e,
    InscripcionTorneo equipo,
    String goleador,
    String asistidor
  ) {
    GolTorneo gol = EstadisticasGoles.completar(new GolTorneo(), goleador, asistidor);
    gol.setEncuentro(e);
    gol.setInscripcion(equipo);
    return gol;
  }
}
