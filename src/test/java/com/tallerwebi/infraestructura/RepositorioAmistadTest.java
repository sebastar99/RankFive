package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Amistad;
import com.tallerwebi.dominio.EstadoAmistad;
import com.tallerwebi.dominio.PerfilJugador;
import com.tallerwebi.dominio.RepositorioAmistad;
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
public class RepositorioAmistadTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioAmistad repositorioAmistad;

  @BeforeEach
  public void init() {
    repositorioAmistad = new RepositorioAmistadImpl(sessionFactory);
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

  private Amistad guardarSolicitud(
    Usuario solicitante,
    Usuario destinatario,
    EstadoAmistad estado
  ) {
    Amistad amistad = new Amistad();
    amistad.setUsuario(solicitante);
    amistad.setAmigo(destinatario);
    amistad.setEstado(estado);
    repositorioAmistad.guardar(amistad);
    return amistad;
  }

  @Test
  @Transactional
  @Rollback
  public void guardarAmistadDeberiaPersistirlaConEstadoPendiente() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 2000);

    Amistad amistad = guardarSolicitud(u1, u2, EstadoAmistad.PENDIENTE);

    assertThat(amistad.getId(), is(notNullValue()));
    Amistad recuperada = repositorioAmistad.buscarPorId(amistad.getId());
    assertThat(recuperada.getEstado(), equalTo(EstadoAmistad.PENDIENTE));
    assertThat(recuperada.getUsuario().getEmail(), equalTo("u1@test.com"));
    assertThat(recuperada.getAmigo().getEmail(), equalTo("u2@test.com"));
  }

  @Test
  @Transactional
  @Rollback
  public void actualizarAmistadDeberiaCambiarElEstado() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 2000);
    Amistad amistad = guardarSolicitud(u1, u2, EstadoAmistad.PENDIENTE);

    amistad.setEstado(EstadoAmistad.ACEPTADA);
    repositorioAmistad.actualizar(amistad);

    Amistad recuperada = repositorioAmistad.buscarPorId(amistad.getId());
    assertThat(recuperada.getEstado(), equalTo(EstadoAmistad.ACEPTADA));
  }

  @Test
  @Transactional
  @Rollback
  public void buscarPorIdInexistenteDeberiaRetornarNull() {
    Amistad resultado = repositorioAmistad.buscarPorId(999L);
    assertThat(resultado, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void buscarRelacionEntreDeberiaEncontrarRelacionBidireccional() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 2000);
    guardarSolicitud(u1, u2, EstadoAmistad.PENDIENTE);

    Amistad encontrada = repositorioAmistad.buscarRelacionEntre(u1.getId(), u2.getId());
    assertThat(encontrada, is(notNullValue()));
    assertThat(encontrada.getUsuario().getId(), equalTo(u1.getId()));

    Amistad encontradaInversa = repositorioAmistad.buscarRelacionEntre(u2.getId(), u1.getId());
    assertThat(encontradaInversa, is(notNullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void buscarRelacionEntreDeberiaRetornarNullSiNoExiste() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 2000);

    Amistad resultado = repositorioAmistad.buscarRelacionEntre(u1.getId(), u2.getId());
    assertThat(resultado, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void buscarRelacionEntreNoDeberiaRetornarRelacionesRechazadas() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 2000);
    guardarSolicitud(u1, u2, EstadoAmistad.RECHAZADA);

    Amistad resultado = repositorioAmistad.buscarRelacionEntre(u1.getId(), u2.getId());
    assertThat(resultado, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void listarAmigosDeDeberiaRetornarAmigosAceptadosEnAmbosSentidos() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 2000);
    Usuario u3 = guardarUsuario("u3@test.com", "user03", 3000);
    guardarSolicitud(u1, u2, EstadoAmistad.ACEPTADA);
    guardarSolicitud(u3, u1, EstadoAmistad.ACEPTADA);
    guardarSolicitud(u1, u3, EstadoAmistad.PENDIENTE);

    List<Usuario> amigos = repositorioAmistad.listarAmigosDe(u1.getId());

    assertThat(amigos, hasSize(2));
    List<String> emails = amigos.stream().map(Usuario::getEmail).toList();
    assertThat(emails, containsInAnyOrder("u2@test.com", "u3@test.com"));
  }

  @Test
  @Transactional
  @Rollback
  public void listarAmigosDeDeberiaRetornarListaVaciaSiNoTieneAmigos() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);

    List<Usuario> amigos = repositorioAmistad.listarAmigosDe(u1.getId());
    assertThat(amigos, hasSize(0));
  }

  @Test
  @Transactional
  @Rollback
  public void listarAmigosDeDeberiaOrdenarPorPuntosDescendente() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 1500);
    Usuario u3 = guardarUsuario("u3@test.com", "user03", 3000);
    guardarSolicitud(u1, u2, EstadoAmistad.ACEPTADA);
    guardarSolicitud(u1, u3, EstadoAmistad.ACEPTADA);

    List<Usuario> amigos = repositorioAmistad.listarAmigosDe(u1.getId());

    assertThat(amigos, hasSize(2));
    assertThat(amigos.get(0).getEmail(), equalTo("u3@test.com"));
    assertThat(amigos.get(1).getEmail(), equalTo("u2@test.com"));
  }

  @Test
  @Transactional
  @Rollback
  public void listarSolicitudesPendientesParaDeberiaRetornarSoloPendientes() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 2000);
    Usuario u3 = guardarUsuario("u3@test.com", "user03", 3000);
    guardarSolicitud(u2, u1, EstadoAmistad.PENDIENTE);
    guardarSolicitud(u3, u1, EstadoAmistad.ACEPTADA);

    List<Amistad> pendientes = repositorioAmistad.listarSolicitudesPendientesPara(u1.getId());

    assertThat(pendientes, hasSize(1));
    assertThat(pendientes.get(0).getUsuario().getEmail(), equalTo("u2@test.com"));
    assertThat(pendientes.get(0).getEstado(), equalTo(EstadoAmistad.PENDIENTE));
  }

  @Test
  @Transactional
  @Rollback
  public void contarSolicitudesPendientesParaDeberiaRetornarCantidadCorrecta() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 2000);
    Usuario u3 = guardarUsuario("u3@test.com", "user03", 3000);
    guardarSolicitud(u2, u1, EstadoAmistad.PENDIENTE);
    guardarSolicitud(u3, u1, EstadoAmistad.PENDIENTE);
    guardarSolicitud(u1, u2, EstadoAmistad.PENDIENTE);

    long count = repositorioAmistad.contarSolicitudesPendientesPara(u1.getId());
    assertThat(count, equalTo(2L));
  }

  @Test
  @Transactional
  @Rollback
  public void contarSolicitudesPendientesParaDeberiaRetornarCeroSiNoTiene() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);

    long count = repositorioAmistad.contarSolicitudesPendientesPara(u1.getId());
    assertThat(count, equalTo(0L));
  }
}
