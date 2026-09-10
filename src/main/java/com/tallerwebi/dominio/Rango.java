package com.tallerwebi.dominio;

public enum Rango {
  BRONCE("Bronce", "rank-bronze", "bi-award", 0),
  PLATA("Plata", "rank-silver", "bi-trophy", 1501),
  ORO("Oro", "rank-gold", "bi-trophy-fill", 2001),
  PLATINO("Platino", "rank-plat", "bi-diamond", 2501),
  DIAMANTE("Diamante", "rank-diamond", "bi-gem", 3001),
  LEGENDARIO("Legendario", "rank-legend", "bi-lightning-charge-fill", 3501);

  private final String nombre;
  private final String css;
  private final String icono;
  private final int minPl;

  Rango(String nombre, String css, String icono, int minPl) {
    this.nombre = nombre;
    this.css = css;
    this.icono = icono;
    this.minPl = minPl;
  }

  public String getNombre() {
    return nombre;
  }

  public String getCss() {
    return css;
  }

  public String getIcono() {
    return icono;
  }

  public int getMinPl() {
    return minPl;
  }

  public static Rango de(Integer puntos) {
    int pl = puntos == null ? 0 : puntos;
    Rango resultado = BRONCE;
    for (Rango rango : values()) {
      if (pl >= rango.minPl) {
        resultado = rango;
      }
    }
    return resultado;
  }
}
