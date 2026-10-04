package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.tallerwebi.dominio.Arbitro;
import com.tallerwebi.dominio.CalificacionArbitro;
import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioArbitro;
import com.tallerwebi.dominio.Torneo;
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
public class RepositorioArbitroTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioArbitro repositorioArbitro;

  @BeforeEach
  public void init() {
    repositorioArbitro = new RepositorioArbitroImpl(sessionFactory);
  }

  private Arbitro guardarArbitro(String nombre) {
    Arbitro arbitro = new Arbitro();
    arbitro.setNombre(nombre);
    repositorioArbitro.guardar(arbitro);
    return arbitro;
  }

  private Usuario guardarUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("1234");
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private EncuentroTorneo guardarEncuentro(Arbitro arbitro) {
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
    fase.setTipo(FaseTorneo.TIPO_ELIMINACION);
    fase.setTorneo(torneo);
    sessionFactory.getCurrentSession().persist(fase);

    EncuentroTorneo encuentro = new EncuentroTorneo();
    encuentro.setFase(fase);
    encuentro.setEstado("JUGADO");
    encuentro.setArbitro(arbitro);
    sessionFactory.getCurrentSession().persist(encuentro);
    return encuentro;
  }

  @Test
  @Transactional
  @Rollback
  public void guardarYBuscarPorIdDevuelveElArbitroPersistidoConPromedioEnCero() {
    Arbitro arbitro = guardarArbitro("Juan Perez");
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Arbitro recuperado = repositorioArbitro.buscarPorId(arbitro.getId());

    assertThat(recuperado, is(notNullValue()));
    assertThat(recuperado.getNombre(), equalTo("Juan Perez"));
    assertThat(recuperado.getPromedio(), equalTo(0.0));
    assertThat(repositorioArbitro.buscarPorId(999L), is(org.hamcrest.Matchers.nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void actualizarPersisteLosCambiosDelArbitro() {
    Arbitro arbitro = guardarArbitro("Carlos Gomez");
    sessionFactory.getCurrentSession().flush();

    arbitro.setPl(1800);
    arbitro.calificar(5);
    repositorioArbitro.actualizar(arbitro);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Arbitro actualizado = repositorioArbitro.buscarPorId(arbitro.getId());
    assertThat(actualizado.getPl(), equalTo(1800));
    assertThat(actualizado.getPromedio(), equalTo(5.0));
  }

  @Test
  @Transactional
  @Rollback
  public void listarTodosDevuelveLosArbitrosOrdenadosPorNombre() {
    guardarArbitro("Zapata");
    guardarArbitro("Acosta");
    sessionFactory.getCurrentSession().flush();

    List<Arbitro> arbitros = repositorioArbitro.listarTodos();

    assertThat(arbitros, hasSize(2));
    assertThat(
      arbitros,
      contains(
        org.hamcrest.Matchers.hasProperty("nombre", equalTo("Acosta")),
        org.hamcrest.Matchers.hasProperty("nombre", equalTo("Zapata"))
      )
    );
  }

  @Test
  @Transactional
  @Rollback
  public void guardarCalificacionYExisteCalificacionParaElUsuarioYEncuentro() {
    Arbitro arbitro = guardarArbitro("Mario Diaz");
    Usuario usuario = guardarUsuario("u@test.com");
    Usuario otroUsuario = guardarUsuario("otro@test.com");
    EncuentroTorneo encuentro = guardarEncuentro(arbitro);
    EncuentroTorneo otroEncuentro = guardarEncuentro(arbitro);

    CalificacionArbitro calificacion = new CalificacionArbitro();
    calificacion.setArbitro(arbitro);
    calificacion.setUsuario(usuario);
    calificacion.setEncuentro(encuentro);
    calificacion.setPuntaje(4);
    repositorioArbitro.guardarCalificacion(calificacion);
    sessionFactory.getCurrentSession().flush();

    assertThat(repositorioArbitro.existeCalificacion(usuario.getId(), encuentro.getId()), is(true));
    assertThat(
      repositorioArbitro.existeCalificacion(otroUsuario.getId(), encuentro.getId()),
      is(false)
    );
    assertThat(
      repositorioArbitro.existeCalificacion(usuario.getId(), otroEncuentro.getId()),
      is(false)
    );
  }

  @Test
  @Transactional
  @Rollback
  public void calificacionesDeLigaSeConsultanPorUsuarioYPartido() {
    Arbitro arbitro = guardarArbitro("Pitana");
    Usuario usuario = guardarUsuario("liga@test.com");
    PartidoLiga partido = guardarPartidoLiga(arbitro, usuario);
    EncuentroTorneo encuentro = guardarEncuentro(arbitro);

    CalificacionArbitro deLiga = new CalificacionArbitro();
    deLiga.setArbitro(arbitro);
    deLiga.setUsuario(usuario);
    deLiga.setPartidoLiga(partido);
    deLiga.setPuntaje(3);
    repositorioArbitro.guardarCalificacion(deLiga);
    CalificacionArbitro deTorneo = new CalificacionArbitro();
    deTorneo.setArbitro(arbitro);
    deTorneo.setUsuario(usuario);
    deTorneo.setEncuentro(encuentro);
    deTorneo.setPuntaje(5);
    repositorioArbitro.guardarCalificacion(deTorneo);
    sessionFactory.getCurrentSession().flush();

    assertThat(
      repositorioArbitro.existeCalificacionLiga(usuario.getId(), partido.getId()),
      is(true)
    );
    assertThat(repositorioArbitro.existeCalificacionLiga(usuario.getId(), 999L), is(false));
    assertThat(
      repositorioArbitro.partidosLigaCalificadosPor(usuario.getId()),
      contains(partido.getId())
    );
    assertThat(
      repositorioArbitro.encuentrosCalificadosPor(usuario.getId()),
      contains(encuentro.getId())
    );
  }

  private PartidoLiga guardarPartidoLiga(Arbitro arbitro, Usuario usuario) {
    Liga liga = new Liga();
    liga.setNombre("Liga Test");
    liga.setFormato(5);
    liga.setCupoEquipos(10);
    liga.setFechaInicio(LocalDate.now());
    liga.setUbicacion("CABA");
    sessionFactory.getCurrentSession().persist(liga);
    InscripcionLiga local = inscripcionLiga(liga, usuario, "Local");
    InscripcionLiga visitante = inscripcionLiga(
      liga,
      guardarUsuario("rival@test.com"),
      "Visitante"
    );
    PartidoLiga partido = new PartidoLiga();
    partido.setLiga(liga);
    partido.setFecha(1);
    partido.setLocal(local);
    partido.setVisitante(visitante);
    partido.setEstado(PartidoLiga.JUGADO);
    partido.setArbitro(arbitro);
    sessionFactory.getCurrentSession().persist(partido);
    return partido;
  }

  private InscripcionLiga inscripcionLiga(Liga liga, Usuario usuario, String nombre) {
    InscripcionLiga insc = new InscripcionLiga();
    insc.setLiga(liga);
    insc.setUsuario(usuario);
    insc.setNombreEquipo(nombre);
    sessionFactory.getCurrentSession().persist(insc);
    return insc;
  }
}
