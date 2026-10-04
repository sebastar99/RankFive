package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.GolLiga;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioGolLiga;
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
public class RepositorioGolLigaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioGolLiga repositorio;
  private InscripcionLiga local;
  private InscripcionLiga visitante;
  private PartidoLiga partido;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioGolLigaImpl(sessionFactory);
    Liga liga = liga("Liga A");
    local = inscripcion(liga, "local@mail.com", "Local FC");
    visitante = inscripcion(liga, "visitante@mail.com", "Visitante FC");
    partido = partido(liga, local, visitante);
  }

  @Test
  public void guardarYListarGolesDeLaLiga() {
    repositorio.guardar(gol(partido, local, "Messi", "Di María"));
    repositorio.guardar(gol(partido, visitante, "Alvarez", null));
    sesion().flush();
    sesion().clear();

    List<GolLiga> goles = repositorio.listarPorLiga(partido.getLiga().getId());

    assertThat(goles, hasSize(2));
    assertThat(goles.get(0).getGoleador(), equalTo("Messi"));
    assertThat(goles.get(0).getAsistidor(), equalTo("Di María"));
    assertThat(goles.get(0).getInscripcion().getNombreVisible(), equalTo("Local FC"));
    assertThat(goles.get(0).getPartido().getId(), equalTo(partido.getId()));
    assertThat(goles.get(1).getAsistidor(), nullValue());
  }

  @Test
  public void listarPorLigaNoIncluyeGolesDeOtrasLigas() {
    Liga otra = liga("Liga B");
    InscripcionLiga a = inscripcion(otra, "a@mail.com", "A");
    InscripcionLiga b = inscripcion(otra, "b@mail.com", "B");
    PartidoLiga otroPartido = partido(otra, a, b);
    repositorio.guardar(gol(partido, local, "Messi", null));
    repositorio.guardar(gol(otroPartido, a, "Otro", null));
    sesion().flush();

    List<GolLiga> goles = repositorio.listarPorLiga(partido.getLiga().getId());

    assertThat(goles, hasSize(1));
    assertThat(goles.get(0).getGoleador(), equalTo("Messi"));
  }

  @Test
  public void contarPorPartidoYEquipo() {
    repositorio.guardar(gol(partido, local, "Messi", null));
    repositorio.guardar(gol(partido, local, "Messi", null));
    repositorio.guardar(gol(partido, visitante, "Alvarez", null));
    sesion().flush();

    assertThat(repositorio.contarPor(partido.getId(), local.getId()), equalTo(2L));
    assertThat(repositorio.contarPor(partido.getId(), visitante.getId()), equalTo(1L));
    assertThat(repositorio.contarPor(partido.getId(), 999L), equalTo(0L));
  }

  @Test
  public void listarLigaSinGolesDevuelveVacio() {
    assertThat(repositorio.listarPorLiga(partido.getLiga().getId()), empty());
  }

  private Session sesion() {
    return sessionFactory.getCurrentSession();
  }

  private Liga liga(String nombre) {
    Liga liga = new Liga();
    liga.setNombre(nombre);
    liga.setFormato(5);
    liga.setCupoEquipos(10);
    liga.setFechaInicio(LocalDate.now());
    liga.setUbicacion("CABA");
    sesion().persist(liga);
    return liga;
  }

  private InscripcionLiga inscripcion(Liga liga, String email, String nombreEquipo) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("123");
    sesion().persist(usuario);
    InscripcionLiga insc = new InscripcionLiga();
    insc.setLiga(liga);
    insc.setUsuario(usuario);
    insc.setNombreEquipo(nombreEquipo);
    sesion().persist(insc);
    return insc;
  }

  private PartidoLiga partido(Liga liga, InscripcionLiga loc, InscripcionLiga vis) {
    PartidoLiga p = new PartidoLiga();
    p.setLiga(liga);
    p.setFecha(1);
    p.setLocal(loc);
    p.setVisitante(vis);
    p.setGolesLocal(2);
    p.setGolesVisitante(1);
    p.setEstado(PartidoLiga.JUGADO);
    sesion().persist(p);
    return p;
  }

  private static GolLiga gol(
    PartidoLiga partido,
    InscripcionLiga equipo,
    String goleador,
    String asistidor
  ) {
    GolLiga gol = new GolLiga();
    gol.setPartido(partido);
    gol.setInscripcion(equipo);
    gol.setGoleador(goleador);
    gol.setAsistidor(asistidor);
    return gol;
  }
}
