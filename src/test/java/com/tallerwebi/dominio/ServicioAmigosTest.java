package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioAmigosImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioAmigosTest {

  private ServicioAmigos servicioAmigos;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.servicioAmigos = new ServicioAmigosImpl(this.repositorioUsuarioMock);
  }

  @Test
  public void buscarAmigoPorNombreDeUsuarioExistente() {
    // preparacion
    String nombreUsuario = "test";
    Usuario usuarioEsperado = new Usuario();
    when(this.repositorioUsuarioMock.buscarPorNombreUsuario(nombreUsuario))
      .thenReturn(usuarioEsperado);
    // ejecucion
    Usuario usuarioObtenido = this.servicioAmigos.buscarAmigoPorNombreDeUsuario(nombreUsuario);
    // validacion
    assertThat(usuarioObtenido, equalTo(usuarioEsperado));
    verify(this.repositorioUsuarioMock, times(1)).buscarPorNombreUsuario(nombreUsuario);
  }

  @Test
  public void buscarAmigoPorNombreDeUsuarioInexistenteDeberiaRetornarNull() {
    // preparacion
    String nombreUsuario = "test";
    when(this.repositorioUsuarioMock.buscarPorNombreUsuario(nombreUsuario)).thenReturn(null);
    // ejecucion
    Usuario usuarioObtenido = this.servicioAmigos.buscarAmigoPorNombreDeUsuario(nombreUsuario);
    // validacion
    assertThat(usuarioObtenido, equalTo(null));
    verify(this.repositorioUsuarioMock, times(1)).buscarPorNombreUsuario(nombreUsuario);
  }

  @Test
  public void buscarAmigoPorNombreUsuarioVacioDeberiaRetornarNull() {
    // preparacion
    String nombreUsuario = "";
    when(this.repositorioUsuarioMock.buscarPorNombreUsuario(nombreUsuario)).thenReturn(null);
    // ejecucion
    Usuario usuarioObtenido = this.servicioAmigos.buscarAmigoPorNombreDeUsuario(nombreUsuario);
    // validacion
    assertThat(usuarioObtenido, equalTo(null));
    verify(this.repositorioUsuarioMock, times(1)).buscarPorNombreUsuario(nombreUsuario);
  }
}
