package com.tallerwebi.dominio;

public enum ResultadoInscripcion {
  INSCRIPTO("Inscripción realizada con éxito"),
  YA_INSCRIPTO("Ya estás inscripto en esta competencia"),
  SIN_EQUIPO_ACTIVO("Necesitás un equipo activo para inscribirte"),
  NO_ES_CAPITAN("Solo el capitán del equipo puede inscribirlo en la competencia"),
  SIN_EQUIPO_COMPATIBLE("Necesitás un equipo activo del mismo formato que la competencia"),
  SELECCIONAR_EQUIPO("Elegí con qué equipo querés inscribirte"),
  EQUIPO_NO_DISPONIBLE(
    "El equipo elegido no es un equipo activo tuyo del formato de la competencia"
  ),
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
