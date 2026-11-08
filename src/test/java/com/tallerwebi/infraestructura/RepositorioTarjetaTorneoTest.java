package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.RepositorioTarjetaTorneo;
import com.tallerwebi.dominio.TarjetaTorneo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.TipoTarjeta;
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
public class RepositorioTarjetaTorneoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioTarjetaTorneo repositorio;
  private Torneo torneo;
  private InscripcionTorneo alfa;
  private InscripcionTorneo beta;
  private EncuentroTorneo encuentro;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioTarjetaTorneoImpl(sessionFactory);
    torneo = torneo("Copa A");
    alfa = inscripcion(torneo, "alfa@mail.com", "Alfa");
    beta = inscripcion(torneo, "beta@mail.com", "Beta");
    encuentro = encuentro(torneo, alfa, beta);
  }

  @Test
  public void guardarYListarTarjetasDelTorneo() {
    repositorio.guardar(tarjeta(encuentro, alfa, TipoTarjeta.AMARILLA));
    repositorio.guardar(tarjeta(encuentro, beta, TipoTarjeta.ROJA));
    sesion().flush();
    sesion().clear();

    List<TarjetaTorneo> tarjetas = repositorio.listarPorTorneo(torneo.getId());

    assertThat(tarjetas, hasSize(2));
    assertThat(tarjetas.get(0).getTipo(), equalTo(TipoTarjeta.AMARILLA));
    assertThat(tarjetas.get(0).getPartidoId(), equalTo(encuentro.getId()));
    assertThat(tarjetas.get(0).getEquipoId(), equalTo(alfa.getId()));
    assertThat(tarjetas.get(1).esRoja(), equalTo(true));
  }

  @Test
  public void borraLasTarjetasDelEncuentroSinTocarLasDeOtros() {
    EncuentroTorneo otro = encuentro(torneo, beta, alfa);
    repositorio.guardar(tarjeta(encuentro, alfa, TipoTarjeta.AMARILLA));
    repositorio.guardar(tarjeta(otro, beta, TipoTarjeta.ROJA));
    sesion().flush();

    repositorio.borrarPorEncuentro(encuentro.getId());
    sesion().flush();
    sesion().clear();

    List<TarjetaTorneo> tarjetas = repositorio.listarPorTorneo(torneo.getId());
    assertThat(tarjetas, hasSize(1));
    assertThat(tarjetas.get(0).getTipo(), equalTo(TipoTarjeta.ROJA));
  }

  @Test
  public void cuentaTarjetasDelUsuarioPorTipo() {
    repositorio.guardar(tarjeta(encuentro, alfa, TipoTarjeta.ROJA));
    repositorio.guardar(tarjeta(encuentro, alfa, TipoTarjeta.AMARILLA));
    sesion().flush();

    Long usuarioId = alfa.getUsuario().getId();
    assertThat(repositorio.contarDe(usuarioId, TipoTarjeta.ROJA), equalTo(1L));
    assertThat(repositorio.contarDe(usuarioId, TipoTarjeta.AMARILLA), equalTo(1L));
    assertThat(repositorio.contarDe(999L, TipoTarjeta.ROJA), equalTo(0L));
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

  private static TarjetaTorneo tarjeta(
    EncuentroTorneo encuentro,
    InscripcionTorneo inscripcion,
    TipoTarjeta tipo
  ) {
    TarjetaTorneo tarjeta = new TarjetaTorneo();
    tarjeta.setEncuentro(encuentro);
    tarjeta.setInscripcion(inscripcion);
    tarjeta.setUsuarioAmonestado(inscripcion.getUsuario());
    tarjeta.setJugador(inscripcion.getUsuario().getEmail());
    tarjeta.setTipo(tipo);
    return tarjeta;
  }
}
