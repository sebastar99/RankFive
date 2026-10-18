package com.tallerwebi.dominio.enums;

public enum ResultadoAceptacionInvitacion {
  ACEPTADA("Te sumaste al equipo"),
  INVITACION_INVALIDA("No se pudo aceptar la invitación"),
  JUGADORES_INSUFICIENTES(
    "No se puede realizar la transferencia porque el equipo no cuenta con la suficiente cantidad de integrantes"
  );

  private final String mensaje;

  ResultadoAceptacionInvitacion(String mensaje) {
    this.mensaje = mensaje;
  }

  public String getMensaje() {
    return mensaje;
  }
}
