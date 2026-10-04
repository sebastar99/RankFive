package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.NotificacionEncuentro;
import com.tallerwebi.dominio.RepositorioNotificacionEncuentro;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.time.LocalDate;
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
public class RepositorioNotificacionEncuentroTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioNotificacionEncuentro repo;

  @BeforeEach
  public void init() {
    repo = new RepositorioNotificacionEncuentroImpl(sessionFactory);
  }

  @Test
  public void guardarYListarNoLeidas() {
    Usuario u = new Usuario();
    u.setEmail("u@test.com");
    u.setPassword("x");
    sessionFactory.getCurrentSession().persist(u);

    Torneo torneo = new Torneo();
    torneo.setNombre("Copa Test");
    torneo.setFormato(5);
    torneo.setCupoEquipos(16);
    torneo.setInscripcionAbierta(true);
    torneo.setFechaInicio(LocalDate.now());
    torneo.setUbicacion("CABA");
    sessionFactory.getCurrentSession().persist(torneo);

    FaseTorneo fase = new FaseTorneo();
    fase.setNumero(1);
    fase.setNombre("Final");
    fase.setTorneo(torneo);
    sessionFactory.getCurrentSession().persist(fase);

    EncuentroTorneo e = new EncuentroTorneo();
    e.setFase(fase);
    e.setEstado("PENDIENTE");
    sessionFactory.getCurrentSession().persist(e);

    NotificacionEncuentro n = new NotificacionEncuentro();
    n.setUsuario(u);
    n.setEncuentro(e);
    n.setMensaje("Partido actualizado");
    repo.guardar(n);

    sessionFactory.getCurrentSession().flush();

    assertThat(repo.listarNoLeidas(u.getId()), hasSize(1));
  }
}
