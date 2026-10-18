package com.tallerwebi.dominio.enums;

public enum ResultadoTransferencia {
  OFERTA_ENVIADA("Oferta enviada. El jugador puede aceptarla o rechazarla"),
  SIN_EQUIPO("Necesitás ser capitán de un equipo de esta competencia"),
  NO_SOS_CAPITAN("Solo el capitán puede ofrecer transferencias"),
  EQUIPO_INVALIDO("El equipo no es válido o no esta inscripto en esta competencia"),
  JUGADOR_NO_PERTENECE("El jugador no forma parte de ese equipo"),
  YA_ES_INTEGRANTE("Ese jugador ya forma parte de tu equipo"),
  OFERTA_YA_PENDIENTE("Ya enviaste una oferta a ese jugador"),
  USUARIO_INVALIDO("Usuario inválido");

  private final String mensaje;

  ResultadoTransferencia(String mensaje) {
    this.mensaje = mensaje;
  }

  public String getMensaje() {
    return mensaje;
  }

  public boolean fueExitoso() {
    return this == OFERTA_ENVIADA;
  }
}
