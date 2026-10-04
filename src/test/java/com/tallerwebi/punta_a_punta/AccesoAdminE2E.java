package com.tallerwebi.punta_a_punta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaNuevoUsuario;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccesoAdminE2E {

  static Playwright playwright;
  static Browser browser;
  BrowserContext context;
  Page page;
  VistaLogin vistaLogin;

  @BeforeAll
  static void abrirNavegador() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch();
  }

  @AfterAll
  static void cerrarNavegador() {
    playwright.close();
  }

  @BeforeEach
  void crearContextoYPagina() {
    ReiniciarDB.limpiarBaseDeDatos();
    context = browser.newContext();
    page = context.newPage();
    vistaLogin = new VistaLogin(page);
  }

  @AfterEach
  void cerrarContexto() {
    context.close();
  }

  @Test
  void usuarioNoAdmin_noPuedeAccederAFormulariosDeCreacion() throws MalformedURLException {
    // Registrar un usuario normal
    vistaLogin.darClickEnRegistrarse();
    VistaNuevoUsuario vistaNuevoUsuario = new VistaNuevoUsuario(context.pages().get(0));
    vistaNuevoUsuario.escribirEMAIL("user-noadmin@rankfive.com");
    vistaNuevoUsuario.escribirClave("123456");
    vistaNuevoUsuario.darClickEnRegistrarme();

    // Volver a login e iniciar sesión
    vistaLogin = new VistaLogin(context.pages().get(0));
    vistaLogin.escribirEMAIL("user-noadmin@rankfive.com");
    vistaLogin.escribirClave("123456");
    vistaLogin.darClickEnIniciarSesion();

    // Ir a /torneos/nuevo -> debe redirigir a /torneos
    page.navigate("localhost:8080/spring/torneos/nuevo");
    URL url1 = new URL(page.url());
    assertThat(url1.getPath(), matchesPattern("^/spring/torneos(?:;jsessionid=[^/\\s]+)?$"));

    // Ir a /ligas/nueva -> debe redirigir a /torneos
    page.navigate("localhost:8080/spring/ligas/nueva");
    URL url2 = new URL(page.url());
    assertThat(url2.getPath(), matchesPattern("^/spring/torneos(?:;jsessionid=[^/\\s]+)?$"));
  }

  @Test
  void usuarioAdmin_puedeAccederAFormulariosDeCreacion() throws MalformedURLException {
    // El ReiniciarDB deja test@unlam.edu.ar como ADMIN con clave 'test'
    vistaLogin.escribirEMAIL("test@unlam.edu.ar");
    vistaLogin.escribirClave("test");
    vistaLogin.darClickEnIniciarSesion();

    // /torneos/nuevo accesible
    page.navigate("localhost:8080/spring/torneos/nuevo");
    URL url1 = new URL(page.url());
    assertThat(url1.getPath(), matchesPattern("^/spring/torneos/nuevo(?:;jsessionid=[^/\\s]+)?$"));

    // /ligas/nueva accesible
    page.navigate("localhost:8080/spring/ligas/nueva");
    URL url2 = new URL(page.url());
    assertThat(url2.getPath(), matchesPattern("^/spring/ligas/nueva(?:;jsessionid=[^/\\s]+)?$"));
  }
}
