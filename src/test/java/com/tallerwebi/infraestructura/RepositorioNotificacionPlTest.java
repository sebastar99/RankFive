package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;

import com.tallerwebi.dominio.NotificacionPl;
import com.tallerwebi.dominio.RepositorioNotificacionPl;
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
public class RepositorioNotificacionPlTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioNotificacionPl repositorioNotificacion;

  @BeforeEach
  public void init() {
    repositorioNotificacion = new RepositorioNotificacionPlImpl(sessionFactory);
  }

  private Usuario guardarUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("1234");
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private NotificacionPl guardarNotificacion(
    Usuario usuario,
    int delta,
    boolean leida,
    LocalDateTime fecha
  ) {
    NotificacionPl notificacion = new NotificacionPl();
    notificacion.setUsuario(usuario);
    notificacion.setDelta(delta);
    notificacion.setLeida(leida);
    notificacion.setFecha(fecha);
    repositorioNotificacion.guardar(notificacion);
    return notificacion;
  }

  @Test
  @Transactional
  @Rollback
  public void listarNoLeidasDeDevuelveSoloLasNoLeidasDelUsuarioOrdenadasPorFechaDesc() {
    Usuario usuario = guardarUsuario("u@test.com");
    Usuario otro = guardarUsuario("o@test.com");

    NotificacionPl reciente = guardarNotificacion(usuario, 10, false, LocalDateTime.now());
    NotificacionPl antigua = guardarNotificacion(
      usuario,
      -5,
      false,
      LocalDateTime.now().minusDays(1)
    );
    guardarNotificacion(usuario, 20, true, LocalDateTime.now());
    guardarNotificacion(otro, 15, false, LocalDateTime.now());
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    List<NotificacionPl> noLeidas = repositorioNotificacion.listarNoLeidasDe(usuario.getId());

    assertThat(noLeidas, hasSize(2));
    assertThat(
      noLeidas,
      contains(
        hasProperty("id", equalTo(reciente.getId())),
        hasProperty("id", equalTo(antigua.getId()))
      )
    );
    assertThat(noLeidas, everyItem(hasProperty("leida", equalTo(false))));
  }

  @Test
  @Transactional
  @Rollback
  public void marcarLeidasDeActualizaSoloLasNoLeidasDelUsuario() {
    Usuario usuario = guardarUsuario("u@test.com");
    Usuario otro = guardarUsuario("o@test.com");
    guardarNotificacion(usuario, 10, false, LocalDateTime.now());
    guardarNotificacion(usuario, 20, false, LocalDateTime.now());
    NotificacionPl deOtro = guardarNotificacion(otro, 5, false, LocalDateTime.now());
    sessionFactory.getCurrentSession().flush();

    repositorioNotificacion.marcarLeidasDe(usuario.getId());
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    assertThat(repositorioNotificacion.listarNoLeidasDe(usuario.getId()), hasSize(0));
    assertThat(
      repositorioNotificacion.listarDe(otro.getId()).get(0).getLeida(),
      equalTo(deOtro.getLeida())
    );
  }

  @Test
  @Transactional
  @Rollback
  public void listarDeDevuelveTodasLasNotificacionesDelUsuarioOrdenadasPorFechaDesc() {
    Usuario usuario = guardarUsuario("u@test.com");
    Usuario otro = guardarUsuario("o@test.com");

    NotificacionPl reciente = guardarNotificacion(usuario, 30, true, LocalDateTime.now());
    NotificacionPl antigua = guardarNotificacion(
      usuario,
      -10,
      false,
      LocalDateTime.now().minusHours(5)
    );
    guardarNotificacion(otro, 1, false, LocalDateTime.now());
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    List<NotificacionPl> todas = repositorioNotificacion.listarDe(usuario.getId());

    assertThat(todas, hasSize(2));
    assertThat(
      todas,
      contains(
        hasProperty("id", equalTo(reciente.getId())),
        hasProperty("id", equalTo(antigua.getId()))
      )
    );
    assertThat(todas.get(0).getMensaje(), equalTo("+30 PL"));
    assertThat(todas.get(1).getMensaje(), equalTo("-10 PL"));
  }
}
