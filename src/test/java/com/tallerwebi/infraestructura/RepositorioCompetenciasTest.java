package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.RepositorioEncuentroTorneo;
import com.tallerwebi.dominio.RepositorioFaseTorneo;
import com.tallerwebi.dominio.RepositorioInscripcionLiga;
import com.tallerwebi.dominio.RepositorioPartidoLiga;
import com.tallerwebi.dominio.RepositorioTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.time.LocalDate;
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
@Transactional
public class RepositorioCompetenciasTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioInscripcionLiga repoInsLiga;
  private RepositorioPartidoLiga repoPartLiga;
  private RepositorioTorneo repoTorneo;
  private RepositorioFaseTorneo repoFase;
  private RepositorioEncuentroTorneo repoEncuentro;

  @BeforeEach
  public void init() {
    repoInsLiga = new RepositorioInscripcionLigaImpl(sessionFactory);
    repoPartLiga = new RepositorioPartidoLigaImpl(sessionFactory);
    repoTorneo = new RepositorioTorneoImpl(sessionFactory);
    repoFase = new RepositorioFaseTorneoImpl(sessionFactory);
    repoEncuentro = new RepositorioEncuentroTorneoImpl(sessionFactory);
  }

  @Test
  public void persisteYListaInscripcionesDeLiga() {
    Liga l = new Liga();
    l.setNombre("Liga Test");
    l.setFormato(5);
    l.setCupoEquipos(10);
    l.setFechaInicio(LocalDate.now());
    l.setUbicacion("CABA");
    sessionFactory.getCurrentSession().save(l);

    com.tallerwebi.dominio.Usuario u = new com.tallerwebi.dominio.Usuario();
    u.setEmail("u@test.com");
    u.setPassword("x");
    sessionFactory.getCurrentSession().persist(u);

    InscripcionLiga ins = new InscripcionLiga();
    ins.setLiga(l);
    ins.setUsuario(u);
    ins.setNombreEquipo("Equipo X");
    repoInsLiga.guardar(ins);

    assertThat(repoInsLiga.listarPorLiga(l.getId()), hasSize(1));
  }

  @Test
  public void persisteFaseEncuentrosYTorneo() {
    Torneo t = new Torneo();
    t.setNombre("Copa Test");
    t.setFormato(5);
    t.setCupoEquipos(16);
    t.setInscripcionAbierta(true);
    t.setFechaInicio(LocalDate.now());
    t.setUbicacion("Lanus");
    repoTorneo.guardar(t);

    FaseTorneo fase = new FaseTorneo();
    fase.setNumero(1);
    fase.setNombre("Grupos");
    fase.setTipo(FaseTorneo.TIPO_GRUPOS);
    fase.setTorneo(t);
    repoFase.guardar(fase);

    com.tallerwebi.dominio.Usuario ua = new com.tallerwebi.dominio.Usuario();
    ua.setEmail("a@test.com");
    ua.setPassword("x");
    sessionFactory.getCurrentSession().persist(ua);
    InscripcionTorneo a = new InscripcionTorneo();
    a.setNombreEquipo("A");
    a.setTorneo(t);
    a.setUsuario(ua);
    com.tallerwebi.dominio.Usuario ub = new com.tallerwebi.dominio.Usuario();
    ub.setEmail("b@test.com");
    ub.setPassword("x");
    sessionFactory.getCurrentSession().persist(ub);
    InscripcionTorneo b = new InscripcionTorneo();
    b.setNombreEquipo("B");
    b.setTorneo(t);
    b.setUsuario(ub);
    sessionFactory.getCurrentSession().persist(a);
    sessionFactory.getCurrentSession().persist(b);

    EncuentroTorneo enc = new EncuentroTorneo();
    enc.setFase(fase);
    enc.setParticipanteA(a);
    enc.setParticipanteB(b);
    enc.setEstado("PENDIENTE");
    repoEncuentro.guardar(enc);
    sessionFactory.getCurrentSession().flush();
    List<FaseTorneo> fases = repoFase.listarPorTorneo(t.getId());
    assertThat(fases, hasSize(1));
  }
}
