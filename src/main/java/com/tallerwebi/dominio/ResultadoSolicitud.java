package com.tallerwebi.dominio;

public enum ResultadoSolicitud {
  ENVIADA("Solicitud de amistad enviada"),
  YA_SON_AMIGOS("Ya son amigos"),
  SOLICITUD_YA_ENVIADA("Ya enviaste una solicitud a este usuario"),
  SOLICITUD_RECIBIDA_PENDIENTE("Este usuario ya te envió una solicitud, revisá tus notificaciones"),
  ES_UNO_MISMO("No podés agregarte a vos mismo"),
  USUARIO_INVALIDO("Usuario no encontrado");

  private final String mensaje;

  ResultadoSolicitud(String mensaje) {
    this.mensaje = mensaje;
  }

  public String getMensaje() {
    return mensaje;
  }

  public boolean fueExitosa() {
    return this == ENVIADA;
  }
}
