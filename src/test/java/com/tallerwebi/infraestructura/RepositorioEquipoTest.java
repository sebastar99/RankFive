package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadoEquipo;
import com.tallerwebi.dominio.EstadoInvitacionEquipo;
import com.tallerwebi.dominio.InvitacionEquipo;
import com.tallerwebi.dominio.PerfilJugador;
import com.tallerwebi.dominio.RepositorioEquipo;
import com.tallerwebi.dominio.RepositorioInvitacionEquipo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
public class RepositorioEquipoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioEquipo repositorioEquipo;
  private RepositorioInvitacionEquipo repositorioInvitacion;

  @BeforeEach
  public void init() {
    repositorioEquipo = new RepositorioEquipoImpl(sessionFactory);
    repositorioInvitacion = new RepositorioInvitacionEquipoImpl(sessionFactory);
  }

  private Usuario guardarUsuario(String email, String nombreUsuario, Integer pl) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("1234");
    usuario.setRol("USER");
    PerfilJugador perfil = new PerfilJugador();
    perfil.setNombreUsuario(nombreUsuario);
    perfil.setPl(pl);
    perfil.setFechaNacimiento(LocalDate.of(2000, 1, 1));
    usuario.setPerfil(perfil);
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Equipo guardarEquipo(Usuario owner, EstadoEquipo estado, Usuario... jugadores) {
    Equipo equipo = new Equipo();
    equipo.setNombre("Equipo de " + owner.getEmail());
    equipo.setOwner(owner);
    equipo.setFormato(5);
    equipo.setEstado(estado);
    equipo.agregarJugador(owner);
    for (Usuario jugador : jugadores) {
      equipo.agregarJugador(jugador);
    }
    repositorioEquipo.guardar(equipo);
    return equipo;
  }

  private InvitacionEquipo guardarInvitacion(
    Equipo equipo,
    Usuario usuario,
    EstadoInvitacionEquipo estado
  ) {
    InvitacionEquipo invitacion = new InvitacionEquipo();
    invitacion.setEquipo(equipo);
    invitacion.setUsuario(usuario);
    invitacion.setEstado(estado);
    repositorioInvitacion.guardar(invitacion);
    return invitacion;
  }

  @Test
  @Transactional
  @Rollback
  public void guardarEquipoDeberiaPersistirloConOwnerYJugadores() {
    Usuario owner = guardarUsuario("o@test.com", "owner", 1500);
    Usuario amigo = guardarUsuario("a@test.com", "amigo", 2500);

    Equipo equipo = guardarEquipo(owner, EstadoEquipo.PENDIENTE, amigo);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Equipo recuperado = repositorioEquipo.buscarPorId(equipo.getId());
    assertThat(recuperado, is(notNullValue()));
    assertThat(recuperado.getOwner().getEmail(), equalTo("o@test.com"));
    assertThat(recuperado.getJugadores(), hasSize(2));
    assertThat(recuperado.getEstado(), equalTo(EstadoEquipo.PENDIENTE));
    assertThat(recuperado.getPromedioPl(), equalTo(2000));
  }

  @Test
  @Transactional
  @Rollback
  public void actualizarEquipoDeberiaCambiarElEstado() {
    Usuario owner = guardarUsuario("o@test.com", "owner", 1500);
    Equipo equipo = guardarEquipo(owner, EstadoEquipo.PENDIENTE);

    equipo.setEstado(EstadoEquipo.ACTIVO);
    repositorioEquipo.actualizar(equipo);
    sessionFactory.getCurrentSession().flush();

    assertThat(repositorioEquipo.buscarPorId(equipo.getId()).estaActivo(), is(true));
  }

  @Test
  @Transactional
  @Rollback
  public void listarActivosDeSoloDevuelveEquiposActivosDondeParticipaElUsuario() {
    Usuario owner = guardarUsuario("o@test.com", "owner", 1500);
    Usuario amigo = guardarUsuario("a@test.com", "amigo", 2500);
    Usuario ajeno = guardarUsuario("x@test.com", "ajeno", 2000);

    Equipo activoConAmigo = guardarEquipo(owner, EstadoEquipo.ACTIVO, amigo);
    guardarEquipo(owner, EstadoEquipo.PENDIENTE, amigo);
    guardarEquipo(ajeno, EstadoEquipo.ACTIVO);
    sessionFactory.getCurrentSession().flush();

    List<Equipo> deAmigo = repositorioEquipo.listarActivosDe(amigo.getId());
    List<Equipo> deAjeno = repositorioEquipo.listarActivosDe(ajeno.getId());

    assertThat(deAmigo, hasSize(1));
    assertThat(deAmigo.get(0).getId(), equalTo(activoConAmigo.getId()));
    assertThat(deAjeno, hasSize(1));
    assertThat(repositorioEquipo.listarActivosDe(999L), hasSize(0));
  }

  @Test
  @Transactional
  @Rollback
  public void invitacionesPendientesSeListanYCuentanPorDestinatario() {
    Usuario owner = guardarUsuario("o@test.com", "owner", 1500);
    Usuario amigo = guardarUsuario("a@test.com", "amigo", 2500);
    Usuario otro = guardarUsuario("b@test.com", "otro", 2000);
    Equipo equipo = guardarEquipo(owner, EstadoEquipo.PENDIENTE);

    InvitacionEquipo pendiente = guardarInvitacion(equipo, amigo, EstadoInvitacionEquipo.PENDIENTE);
    guardarInvitacion(equipo, otro, EstadoInvitacionEquipo.ACEPTADA);
    sessionFactory.getCurrentSession().flush();

    List<InvitacionEquipo> deAmigo = repositorioInvitacion.listarPendientesPara(amigo.getId());
    assertThat(deAmigo, hasSize(1));
    assertThat(deAmigo.get(0).getId(), equalTo(pendiente.getId()));
    assertThat(deAmigo.get(0).getEquipo().getOwner().getEmail(), equalTo("o@test.com"));
    assertThat(repositorioInvitacion.contarPendientesPara(amigo.getId()), equalTo(1L));
    assertThat(repositorioInvitacion.contarPendientesPara(otro.getId()), equalTo(0L));
    assertThat(repositorioInvitacion.listarDeEquipo(equipo.getId()), hasSize(2));
  }

  @Test
  @Transactional
  @Rollback
  public void buscarYActualizarInvitacion() {
    Usuario owner = guardarUsuario("o@test.com", "owner", 1500);
    Usuario amigo = guardarUsuario("a@test.com", "amigo", 2500);
    Equipo equipo = guardarEquipo(owner, EstadoEquipo.PENDIENTE);
    InvitacionEquipo invitacion = guardarInvitacion(
      equipo,
      amigo,
      EstadoInvitacionEquipo.PENDIENTE
    );

    invitacion.setEstado(EstadoInvitacionEquipo.RECHAZADA);
    repositorioInvitacion.actualizar(invitacion);
    sessionFactory.getCurrentSession().flush();

    InvitacionEquipo recuperada = repositorioInvitacion.buscarPorId(invitacion.getId());
    assertThat(recuperada.getEstado(), equalTo(EstadoInvitacionEquipo.RECHAZADA));
    assertThat(recuperada.fueEnviadaA(amigo.getId()), is(true));
    assertThat(repositorioInvitacion.buscarPorId(999L), is(nullValue()));
  }
}
