package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.PerfilJugador;
import com.tallerwebi.dominio.RepositorioPartido;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
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
public class RepositorioPartidoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPartido repositorioPartido;

  @BeforeEach
  public void init() {
    repositorioPartido = new RepositorioPartidoImpl(sessionFactory);
  }

  private Usuario guardarUsuario(String email, String nombreUsuario, Integer pl) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("1234");
    usuario.setRol("USER");
    PerfilJugador perfil = new PerfilJugador();
    perfil.setNombreUsuario(nombreUsuario);
    perfil.setPl(pl);
    usuario.setPerfil(perfil);
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Partido guardarPartido(Usuario jugadorA, Usuario jugadorB, LocalDateTime fecha) {
    Partido partido = new Partido();
    partido.getEquipoA().add(jugadorA);
    partido.getEquipoB().add(jugadorB);
    partido.setFecha(fecha);
    repositorioPartido.guardar(partido);
    return partido;
  }

  @Test
  @Transactional
  @Rollback
  public void guardarPartidoDeberiaPersistirloConSusEquipos() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 1200);

    Partido partido = guardarPartido(u1, u2, LocalDateTime.now());

    assertThat(partido.getId(), is(notNullValue()));
    Partido recuperado = repositorioPartido.buscarPorId(partido.getId());
    assertThat(recuperado.getEquipoA().iterator().next().getEmail(), equalTo("u1@test.com"));
    assertThat(recuperado.getEquipoB().iterator().next().getEmail(), equalTo("u2@test.com"));
    assertThat(recuperado.fueJugado(), is(false));
  }

  @Test
  @Transactional
  @Rollback
  public void actualizarPartidoDeberiaGuardarElResultado() {
    Partido partido = new Partido();
    repositorioPartido.guardar(partido);

    partido.registrarResultado(2, 1);
    repositorioPartido.actualizar(partido);

    Partido recuperado = repositorioPartido.buscarPorId(partido.getId());
    assertThat(recuperado.getGolesEquipoA(), equalTo(2));
    assertThat(recuperado.getGolesEquipoB(), equalTo(1));
  }

  @Test
  @Transactional
  @Rollback
  public void buscarPorIdInexistenteDeberiaRetornarNull() {
    Partido resultado = repositorioPartido.buscarPorId(999L);
    assertThat(resultado, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void listarPartidosDeDeberiaEncontrarPartidosEnCualquierEquipo() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 1200);
    Usuario u3 = guardarUsuario("u3@test.com", "user03", 1500);
    Partido partidoComoEquipoA = guardarPartido(u1, u2, LocalDateTime.now().minusDays(1));
    Partido partidoComoEquipoB = guardarPartido(u3, u1, LocalDateTime.now().minusDays(2));
    guardarPartido(u2, u3, LocalDateTime.now());

    List<Partido> partidos = repositorioPartido.listarPartidosDe(u1.getId());

    assertThat(partidos, hasSize(2));
    List<Long> ids = partidos.stream().map(Partido::getId).toList();
    assertThat(ids, contains(partidoComoEquipoA.getId(), partidoComoEquipoB.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void listarPartidosDeDeberiaOrdenarPorFechaDescendente() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);
    Usuario u2 = guardarUsuario("u2@test.com", "user02", 1200);
    Partido masViejo = guardarPartido(u1, u2, LocalDateTime.now().minusDays(5));
    Partido masNuevo = guardarPartido(u1, u2, LocalDateTime.now());

    List<Partido> partidos = repositorioPartido.listarPartidosDe(u1.getId());

    assertThat(partidos, hasSize(2));
    assertThat(partidos.get(0).getId(), equalTo(masNuevo.getId()));
    assertThat(partidos.get(1).getId(), equalTo(masViejo.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void listarPartidosDeDeberiaRetornarListaVaciaSiNoJugoNinguno() {
    Usuario u1 = guardarUsuario("u1@test.com", "user01", 1000);

    List<Partido> partidos = repositorioPartido.listarPartidosDe(u1.getId());

    assertThat(partidos, hasSize(0));
  }
}
