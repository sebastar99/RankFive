package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadoEquipo;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.RepositorioInscripcionTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioInscripcionTorneoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioInscripcionTorneo repositorioInscripcion;

  @BeforeEach
  public void init() {
    repositorioInscripcion = new RepositorioInscripcionTorneoImpl(sessionFactory);
  }

  private Usuario guardarUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("1234");
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Torneo guardarTorneo(String nombre) {
    Torneo torneo = new Torneo();
    torneo.setNombre(nombre);
    torneo.setFormato(5);
    torneo.setCupoEquipos(16);
    torneo.setInscripcionAbierta(true);
    torneo.setFechaInicio(LocalDate.now());
    torneo.setUbicacion("CABA");
    sessionFactory.getCurrentSession().persist(torneo);
    return torneo;
  }

  private Equipo guardarEquipo(String nombre, Usuario owner) {
    Equipo equipo = new Equipo();
    equipo.setNombre(nombre);
    equipo.setOwner(owner);
    equipo.setFormato(5);
    equipo.setEstado(EstadoEquipo.ACTIVO);
    equipo.agregarJugador(owner);
    sessionFactory.getCurrentSession().persist(equipo);
    return equipo;
  }

  private InscripcionTorneo guardarInscripcion(
    Torneo torneo,
    Usuario usuario,
    Equipo equipo,
    LocalDateTime fecha
  ) {
    InscripcionTorneo inscripcion = new InscripcionTorneo();
    inscripcion.setTorneo(torneo);
    inscripcion.setUsuario(usuario);
    inscripcion.setEquipo(equipo);
    inscripcion.setNombreEquipo(equipo != null ? equipo.getNombre() : usuario.getEmail());
    inscripcion.setFechaInscripcion(fecha);
    repositorioInscripcion.guardar(inscripcion);
    return inscripcion;
  }

  @Test
  @Transactional
  @Rollback
  public void existeParaDevuelveTrueSoloParaElParDeUsuarioYTorneoInscripto() {
    Usuario usuario = guardarUsuario("u@test.com");
    Torneo torneo = guardarTorneo("Copa A");
    Torneo otroTorneo = guardarTorneo("Copa B");
    guardarInscripcion(torneo, usuario, null, LocalDateTime.now());
    sessionFactory.getCurrentSession().flush();

    assertThat(repositorioInscripcion.existePara(usuario.getId(), torneo.getId()), is(true));
    assertThat(repositorioInscripcion.existePara(usuario.getId(), otroTorneo.getId()), is(false));
    assertThat(repositorioInscripcion.existePara(999L, torneo.getId()), is(false));
  }

  @Test
  @Transactional
  @Rollback
  public void listarPorTorneoDevuelveSoloLasDelTorneoOrdenadasPorFecha() {
    Usuario primero = guardarUsuario("primero@test.com");
    Usuario segundo = guardarUsuario("segundo@test.com");
    Usuario ajeno = guardarUsuario("ajeno@test.com");
    Torneo torneo = guardarTorneo("Copa A");
    Torneo otroTorneo = guardarTorneo("Copa B");

    InscripcionTorneo tardia = guardarInscripcion(
      torneo,
      segundo,
      null,
      LocalDateTime.now().plusDays(1)
    );
    InscripcionTorneo temprana = guardarInscripcion(
      torneo,
      primero,
      null,
      LocalDateTime.now().minusDays(1)
    );
    guardarInscripcion(otroTorneo, ajeno, null, LocalDateTime.now());
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    List<InscripcionTorneo> inscripciones = repositorioInscripcion.listarPorTorneo(torneo.getId());

    assertThat(inscripciones, hasSize(2));
    assertThat(
      inscripciones,
      contains(hasIdEqualTo(temprana.getId()), hasIdEqualTo(tardia.getId()))
    );
  }

  private static org.hamcrest.Matcher<InscripcionTorneo> hasIdEqualTo(Long id) {
    return org.hamcrest.Matchers.hasProperty("id", equalTo(id));
  }

  @Test
  @Transactional
  @Rollback
  public void listarEquiposDevuelveLosEquiposDistintosInscriptosEnElTorneoOrdenadosPorNombre() {
    Usuario ownerZ = guardarUsuario("z@test.com");
    Usuario ownerA = guardarUsuario("a@test.com");
    Usuario sinEquipo = guardarUsuario("sin-equipo@test.com");
    Torneo torneo = guardarTorneo("Copa A");
    Torneo otroTorneo = guardarTorneo("Copa B");

    Equipo equipoZ = guardarEquipo("Zeta FC", ownerZ);
    Equipo equipoA = guardarEquipo("Alfa FC", ownerA);

    guardarInscripcion(torneo, ownerZ, equipoZ, LocalDateTime.now());
    guardarInscripcion(torneo, ownerA, equipoA, LocalDateTime.now());
    guardarInscripcion(torneo, sinEquipo, null, LocalDateTime.now());
    guardarInscripcion(otroTorneo, ownerA, equipoA, LocalDateTime.now());
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    List<Equipo> equipos = repositorioInscripcion.listarEquipos(torneo.getId());

    assertThat(equipos, hasSize(2));
    assertThat(equipos.get(0).getNombre(), equalTo("Alfa FC"));
    assertThat(equipos.get(1).getNombre(), equalTo("Zeta FC"));
  }
}
