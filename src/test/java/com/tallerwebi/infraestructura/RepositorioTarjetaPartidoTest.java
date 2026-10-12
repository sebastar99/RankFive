package com.tallerwebi.infraestructura;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.hibernate.SessionFactory;

import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioTarjetaPartido;
import com.tallerwebi.dominio.TarjetaPartido;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.TipoTarjeta;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;

import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HibernateInfraestructuraTestConfig.class)
public class RepositorioTarjetaPartidoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioTarjetaPartido repositorio;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioTarjetaPartidoImpl(sessionFactory);
  }

  private Usuario guardarUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("1234");
    usuario.setRol("USER");
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Partido guardarPartido(Usuario jugador) {
    Partido partido = new Partido();
    partido.getEquipoA().add(jugador);
    partido.registrarResultado(2, 0);
    sessionFactory.getCurrentSession().persist(partido);
    return partido;
  }

  private org.hibernate.Session sesion() {
    return sessionFactory.getCurrentSession();
  }


    @Test
    @Transactional 
    public void deberiaGuardarUnaTarjetaAmarilla() {
    Usuario amonestado = guardarUsuario("amonestado@test.com");
    Partido partido = guardarPartido(amonestado);

    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(amonestado);
    tarjeta.setJugador("amonestado@test.com");  
    tarjeta.setTipo(TipoTarjeta.AMARILLA);

    repositorio.guardar(tarjeta);

    sesion().flush();
    sesion().clear();

    // Acción
    List<TarjetaPartido> tarjetas = repositorio.listarTarjetasPorPartido(partido.getId());

    // Verificación
    assertEquals(1, tarjetas.size());
    TarjetaPartido recuperada = tarjetas.get(0);
    assertEquals(amonestado.getId(), recuperada.getUsuarioAmonestado().getId());
    assertEquals(TipoTarjeta.AMARILLA, recuperada.getTipo());
  }

  @Test 
  @Transactional 
  public void deberiaGuardarUnaTarjetaRoja() {
    Usuario amonestado = guardarUsuario("amonestado@test.com");
    Partido partido = guardarPartido(amonestado);

    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(amonestado);
    tarjeta.setJugador("amonestado@test.com");  
    tarjeta.setTipo(TipoTarjeta.ROJA);

    repositorio.guardar(tarjeta);

    sesion().flush();
    sesion().clear();

    // Acción
    List<TarjetaPartido> tarjetas = repositorio.listarTarjetasPorPartido(partido.getId());

    // Verificación
    assertEquals(1, tarjetas.size());
    TarjetaPartido recuperada = tarjetas.get(0);
    assertEquals(amonestado.getId(), recuperada.getUsuarioAmonestado().getId());
    assertEquals(TipoTarjeta.ROJA, recuperada.getTipo());
  }
    
  @Test 
  @Transactional 
  public void deberiaBorrarLasTarjetasDeUnPartido() {
    Usuario amonestado = guardarUsuario("amonestado@test.com");
    Partido partido = guardarPartido(amonestado);

    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(amonestado);
    tarjeta.setJugador("amonestado@test.com");  
    tarjeta.setTipo(TipoTarjeta.ROJA);

    repositorio.guardar(tarjeta);

    sesion().flush();
    sesion().clear();

    // Acción
    repositorio.borrarPorPartido(partido.getId());

    sesion().flush();
    sesion().clear();

    // Verificación
    List<TarjetaPartido> tarjetas = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(0, tarjetas.size());
  }
    
  @Test 
  @Transactional 
  public void deberiaBorrarLasTarjetasDeUnPartidoYGuardarOtras() {
    Usuario amonestado = guardarUsuario("amonestado@test.com");
    Partido partido = guardarPartido(amonestado);

    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(amonestado);
    tarjeta.setJugador("amonestado@test.com");  
    tarjeta.setTipo(TipoTarjeta.ROJA);

    repositorio.guardar(tarjeta);

    sesion().flush();
    sesion().clear();

    // Acción
    repositorio.borrarPorPartido(partido.getId());

    sesion().flush();
    sesion().clear();

    // Verificación
    List<TarjetaPartido> tarjetas = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(0, tarjetas.size());
    
    // Acción
    TarjetaPartido tarjeta2 = new TarjetaPartido();
    tarjeta2.setPartido(partido);
    tarjeta2.setUsuarioAmonestado(amonestado);
    tarjeta2.setJugador("amonestado@test.com");  
    tarjeta2.setTipo(TipoTarjeta.AMARILLA);

    repositorio.guardar(tarjeta2);

    sesion().flush();
    sesion().clear();

    // Verificación
    List<TarjetaPartido> tarjetas2 = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(1, tarjetas2.size());
    TarjetaPartido recuperada2 = tarjetas2.get(0);
    assertEquals(amonestado.getId(), recuperada2.getUsuarioAmonestado().getId());
    assertEquals(TipoTarjeta.AMARILLA, recuperada2.getTipo());
  }

  @Test 
  @Transactional 
  public void deberiaBorrarLasTarjetasDeUnPartidoYGuardarOtrasConDistintoTipo() {
    Usuario amonestado = guardarUsuario("amonestado@test.com");
    Partido partido = guardarPartido(amonestado);

    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(amonestado);
    tarjeta.setJugador("amonestado@test.com");  
    tarjeta.setTipo(TipoTarjeta.ROJA);

    repositorio.guardar(tarjeta);

    sesion().flush();
    sesion().clear();

    // Acción
    repositorio.borrarPorPartido(partido.getId());

    sesion().flush();
    sesion().clear();

    // Verificación
    List<TarjetaPartido> tarjetas = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(0, tarjetas.size());
    
    // Acción
    TarjetaPartido tarjeta2 = new TarjetaPartido();
    tarjeta2.setPartido(partido);
    tarjeta2.setUsuarioAmonestado(amonestado);
    tarjeta2.setJugador("amonestado@test.com");  
    tarjeta2.setTipo(TipoTarjeta.AMARILLA);

    repositorio.guardar(tarjeta2);

    sesion().flush();
    sesion().clear();

    // Verificación
    List<TarjetaPartido> tarjetas2 = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(1, tarjetas2.size());
    TarjetaPartido recuperada2 = tarjetas2.get(0);
    assertEquals(amonestado.getId(), recuperada2.getUsuarioAmonestado().getId());
    assertEquals(TipoTarjeta.AMARILLA, recuperada2.getTipo());
  }

  @Test 
  @Transactional 
  public void deberiaBorrarLasTarjetasDeUnPartidoYGuardarOtrasConDistintoJugador() {
    Usuario amonestado = guardarUsuario("amonestado@test.com");
    Usuario amonestado2 = guardarUsuario("amonestado2@test.com");
    Partido partido = guardarPartido(amonestado);

    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(amonestado);
    tarjeta.setJugador("amonestado@test.com");  
    tarjeta.setTipo(TipoTarjeta.ROJA);

    repositorio.guardar(tarjeta);

    sesion().flush();
    sesion().clear();

    // Acción
    repositorio.borrarPorPartido(partido.getId());

    sesion().flush();
    sesion().clear();

    // Verificación
    List<TarjetaPartido> tarjetas = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(0, tarjetas.size());
    
    // Acción
    TarjetaPartido tarjeta2 = new TarjetaPartido();
    tarjeta2.setPartido(partido);
    tarjeta2.setUsuarioAmonestado(amonestado2);
    tarjeta2.setJugador("amonestado2@test.com");  
    tarjeta2.setTipo(TipoTarjeta.AMARILLA);

    repositorio.guardar(tarjeta2);

    sesion().flush();
    sesion().clear();

    // Verificación
    List<TarjetaPartido> tarjetas2 = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(1, tarjetas2.size());
    TarjetaPartido recuperada2 = tarjetas2.get(0);
    assertEquals(amonestado2.getId(), recuperada2.getUsuarioAmonestado().getId());
    assertEquals(TipoTarjeta.AMARILLA, recuperada2.getTipo());
  }

  @Test 
  @Transactional 
  public void listarTarjetasPorPartidoDeberiaDevolverSoloLasTarjetasDeEsePartido() {
    Usuario amonestado = guardarUsuario("amonestado@test.com");
    Partido partido = guardarPartido(amonestado);
    Partido partido2 = guardarPartido(amonestado);

    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(amonestado);
    tarjeta.setJugador("amonestado@test.com");  
    tarjeta.setTipo(TipoTarjeta.ROJA);

    repositorio.guardar(tarjeta);

    TarjetaPartido tarjeta2 = new TarjetaPartido();
    tarjeta2.setPartido(partido2);
    tarjeta2.setUsuarioAmonestado(amonestado);
    tarjeta2.setJugador("amonestado@test.com");  
    tarjeta2.setTipo(TipoTarjeta.AMARILLA);

    repositorio.guardar(tarjeta2);

    sesion().flush();
    sesion().clear();
    
    // Verificación
    List<TarjetaPartido> tarjetas = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(1, tarjetas.size());
    TarjetaPartido recuperada = tarjetas.get(0);
    assertEquals(amonestado.getId(), recuperada.getUsuarioAmonestado().getId());
    assertEquals(TipoTarjeta.ROJA, recuperada.getTipo());
    
    List<TarjetaPartido> tarjetas2 = repositorio.listarTarjetasPorPartido(partido2.getId());
    assertEquals(1, tarjetas2.size());
    TarjetaPartido recuperada2 = tarjetas2.get(0);
    assertEquals(amonestado.getId(), recuperada2.getUsuarioAmonestado().getId());
    assertEquals(TipoTarjeta.AMARILLA, recuperada2.getTipo());
  }

  @Test
  @Transactional
  public void borrarPorPartidoNoDeberiaEliminarTarjetasDeOtrosPartidos () {
    Usuario amonestado = guardarUsuario("amonestado@test.com");
    Partido partido = guardarPartido(amonestado);
    Partido partido2 = guardarPartido(amonestado);

    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(amonestado);
    tarjeta.setJugador("amonestado@test.com");  
    tarjeta.setTipo(TipoTarjeta.ROJA);

    repositorio.guardar(tarjeta);

    TarjetaPartido tarjeta2 = new TarjetaPartido();
    tarjeta2.setPartido(partido2);
    tarjeta2.setUsuarioAmonestado(amonestado);
    tarjeta2.setJugador("amonestado@test.com");  
    tarjeta2.setTipo(TipoTarjeta.AMARILLA);

    repositorio.guardar(tarjeta2);
    repositorio.borrarPorPartido(partido.getId());
    sesion().flush();
    sesion().clear();
        // Verificación
    List<TarjetaPartido> tarjetas = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(0, tarjetas.size());
    
    List<TarjetaPartido> tarjetas2 = repositorio.listarTarjetasPorPartido(partido2.getId());
    assertEquals(1, tarjetas2.size());
    TarjetaPartido recuperada2 = tarjetas2.get(0);
    assertEquals(amonestado.getId(), recuperada2.getUsuarioAmonestado().getId());
    assertEquals(TipoTarjeta.AMARILLA, recuperada2.getTipo());
  }

  @Test 
  @Transactional 
  public void deberiaGuardarElNombreDelJugador(){
    Usuario amonestado = guardarUsuario("amonestado@test.com");
    Partido partido = guardarPartido(amonestado);

    TarjetaPartido tarjeta = new TarjetaPartido();
    tarjeta.setPartido(partido);
    tarjeta.setUsuarioAmonestado(amonestado);
    tarjeta.setJugador("amonestado@test.com");  
    tarjeta.setTipo(TipoTarjeta.ROJA);

    repositorio.guardar(tarjeta);

    sesion().flush();
    sesion().clear();
    
    // Verificación
    List<TarjetaPartido> tarjetas = repositorio.listarTarjetasPorPartido(partido.getId());
    assertEquals(1, tarjetas.size());
    TarjetaPartido recuperada = tarjetas.get(0);
    assertEquals("amonestado@test.com", recuperada.getJugador());
  }
}
