package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.presentacion.DatosAmigos;
import com.tallerwebi.presentacion.DatosLogin;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class PerfilJugadorTest {

  @Test
  public void getEdadConFechaNacimientoDeberiaCalcularEdadCorrectamente() {
    PerfilJugador perfil = new PerfilJugador();
    perfil.setFechaNacimiento(LocalDate.of(2000, 1, 1));

    Integer edad = perfil.getEdad();

    assertThat(edad, is(notNullValue()));
    assertThat(edad >= 24, is(true));
  }

  @Test
  public void getEdadSinFechaNacimientoDeberiaRetornarNull() {
    PerfilJugador perfil = new PerfilJugador();

    Integer edad = perfil.getEdad();

    assertThat(edad, is(nullValue()));
  }

  @Test
  public void usuarioActivarDeberiaMarcarComoActivo() {
    Usuario usuario = new Usuario();

    assertThat(usuario.getActivo(), equalTo(false));

    usuario.activar();

    assertThat(usuario.getActivo(), equalTo(true));
  }

  @Test
  public void datosAmigosGetterSetterDeberianFuncionar() {
    DatosAmigos datos = new DatosAmigos();
    datos.setNombreUsuario("user01");

    assertThat(datos.getNombreUsuario(), equalTo("user01"));
  }

  @Test
  public void datosLoginNoArgConstructorYGettersSettersDeberianFuncionar() {
    DatosLogin datos = new DatosLogin();
    datos.setEmail("test@test.com");
    datos.setPassword("1234");

    assertThat(datos.getEmail(), equalTo("test@test.com"));
    assertThat(datos.getPassword(), equalTo("1234"));
  }
}
