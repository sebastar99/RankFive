package com.tallerwebi.dominio;

public enum ResultadoCreacionEquipo {
  CREADO("Equipo creado. Esperando confirmación de los jugadores invitados"),
  NOMBRE_INVALIDO("El nombre del equipo es obligatorio"),
  FORMATO_INVALIDO("El formato del equipo no es válido"),
  SIN_JUGADORES("Tenés que invitar al menos a un amigo"),
  JUGADORES_INSUFICIENTES("El formato elegido necesita más jugadores"),
  JUGADOR_NO_AMIGO("Solo podés invitar a tus amigos"),
  USUARIO_INVALIDO("Usuario inválido");

  private final String mensaje;

  ResultadoCreacionEquipo(String mensaje) {
    this.mensaje = mensaje;
  }

  public String getMensaje() {
    return mensaje;
  }

  public boolean fueExitoso() {
    return this == CREADO;
  }
}
