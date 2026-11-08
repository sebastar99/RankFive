package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioTarjetaLiga;
import com.tallerwebi.dominio.TarjetaLiga;
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
public class RepositorioTarjetaLigaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioTarjetaLiga repositorio;
  private InscripcionLiga local;
  private InscripcionLiga visitante;
  private PartidoLiga partido;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioTarjetaLigaImpl(sessionFactory);
    Liga liga = liga("Liga A");
    local = inscripcion(liga, "local@mail.com", "Local FC");
    visitante = inscripcion(liga, "visitante@mail.com", "Visitante FC");
    partido = partido(liga, local, visitante);
  }

  @Test
  public void guardarYListarTarjetasDeLaLiga() {
    repositorio.guardar(tarjeta(partido, local, TipoTarjeta.AMARILLA));
    repositorio.guardar(tarjeta(partido, visitante, TipoTarjeta.ROJA));
    sesion().flush();
    sesion().clear();

    List<TarjetaLiga> tarjetas = repositorio.listarPorLiga(partido.getLiga().getId());

    assertThat(tarjetas, hasSize(2));
    assertThat(tarjetas.get(0).getTipo(), equalTo(TipoTarjeta.AMARILLA));
    assertThat(tarjetas.get(0).getPartidoId(), equalTo(partido.getId()));
    assertThat(tarjetas.get(0).getEquipoId(), equalTo(local.getId()));
    assertThat(tarjetas.get(1).esRoja(), equalTo(true));
  }

  @Test
  public void borraLasTarjetasDelPartidoSinTocarLasDeOtros() {
    PartidoLiga otro = partido(partido.getLiga(), visitante, local);
    repositorio.guardar(tarjeta(partido, local, TipoTarjeta.AMARILLA));
    repositorio.guardar(tarjeta(otro, visitante, TipoTarjeta.ROJA));
    sesion().flush();

    repositorio.borrarPorPartido(partido.getId());
    sesion().flush();
    sesion().clear();

    List<TarjetaLiga> tarjetas = repositorio.listarPorLiga(partido.getLiga().getId());
    assertThat(tarjetas, hasSize(1));
    assertThat(tarjetas.get(0).getTipo(), equalTo(TipoTarjeta.ROJA));
  }

  @Test
  public void cuentaTarjetasDelUsuarioPorTipo() {
    repositorio.guardar(tarjeta(partido, local, TipoTarjeta.AMARILLA));
    repositorio.guardar(tarjeta(partido, local, TipoTarjeta.AMARILLA));
    repositorio.guardar(tarjeta(partido, local, TipoTarjeta.ROJA));
    sesion().flush();

    Long usuarioId = local.getUsuario().getId();
    assertThat(repositorio.contarDe(usuarioId, TipoTarjeta.AMARILLA), equalTo(2L));
    assertThat(repositorio.contarDe(usuarioId, TipoTarjeta.ROJA), equalTo(1L));
    assertThat(repositorio.contarDe(999L, TipoTarjeta.AMARILLA), equalTo(0L));
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

  private static TarjetaLiga tarjeta(
    PartidoLiga partido,
    InscripcionLiga inscripcion,
    TipoTarjeta tipo
  ) {
    TarjetaLiga tarjeta = new TarjetaLiga();
    tarjeta.setPartido(partido);
    tarjeta.setInscripcion(inscripcion);
    tarjeta.setUsuarioAmonestado(inscripcion.getUsuario());
    tarjeta.setJugador(inscripcion.getUsuario().getEmail());
    tarjeta.setTipo(tipo);
    return tarjeta;
  }
}
