package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaLogin extends VistaWeb {

  public VistaLogin(Page page) {
    super(page);
    page.navigate("localhost:8080/spring/login");
  }

  public String obtenerTextoDeLaBarraDeNavegacion() {
    return this.obtenerTextoDelElemento("nav a.navbar-brand");
  }

  public String obtenerMensajeDeError() {
    return this.obtenerTextoDelElemento("p.alert.alert-danger");
  }

  public void escribirEMAIL(String email) {
    page.locator("#email").fill(email);
  }

  public void escribirClave(String clave) {
    page.locator("#password").fill(clave);
  }

  public void darClickEnIniciarSesion() {
    page.evaluate("document.getElementById('btn-login').disabled = false");
    page.evaluate("document.querySelector('form').submit()");
    page.waitForLoadState();
  }

  public void darClickEnRegistrarse() {
    this.darClickEnElElemento("#btn-register");
  }
}
