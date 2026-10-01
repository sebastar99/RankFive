package com.tallerwebi.dominio;

public enum ResultadoInscripcion {
  INSCRIPTO("Inscripción realizada con éxito"),
  YA_INSCRIPTO("Ya estás inscripto en esta competencia"),
  SIN_EQUIPO_ACTIVO("Necesitás un equipo activo para inscribirte"),
  INSCRIPCION_CERRADA("La inscripción está cerrada o el fixture ya fue generado"),
  CUPO_COMPLETO("La competencia ya completó su cupo de equipos"),
  INVALIDO("No se pudo realizar la inscripción");

  private final String mensaje;

  ResultadoInscripcion(String mensaje) {
    this.mensaje = mensaje;
  }

  public String getMensaje() {
    return mensaje;
  }

  public boolean esExitoso() {
    return this == INSCRIPTO;
  }
}
