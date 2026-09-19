package com.tallerwebi.dominio;

public enum RangoEquipo {
  PRINCIPIANTE("Principiante", "principiante", 1000, 1498, "principiante.png", 0),
  AMATEUR("Amateur", "amateur", 1499, 1997, "amateur.png", 1),
  SEMIPROFESIONAL("Semiprofesional", "semiprofesional", 1998, 2496, "semiprofesional.png", 2),
  PROFESIONAL("Profesional", "profesional", 2497, 2995, "profesional.png", 3),
  CLASE_MUNDIAL("Clase Mundial", "clase-mundial", 2996, 3494, "clase-mundial.png", 4),
  LEGENDARIO("Legendario", "legendario", 3495, Integer.MAX_VALUE, "legendario.png", 5);

  private final String nombre;
  private final String slug;
  private final int minInclusive;
  private final int maxInclusive;
  private final String iconoAsset;
  private final int orden;

  RangoEquipo(
    String nombre,
    String slug,
    int minInclusive,
    int maxInclusive,
    String iconoAsset,
    int orden
  ) {
    this.nombre = nombre;
    this.slug = slug;
    this.minInclusive = minInclusive;
    this.maxInclusive = maxInclusive;
    this.iconoAsset = iconoAsset;
    this.orden = orden;
  }

  public String getNombre() {
    return nombre;
  }

  public String getSlug() {
    return slug;
  }

  public int getMinInclusive() {
    return minInclusive;
  }

  public int getMaxInclusive() {
    return maxInclusive;
  }

  public String getIconoAsset() {
    return iconoAsset;
  }

  public int getOrden() {
    return orden;
  }

  public static RangoEquipo dePromedio(int promedioPl) {
    int pl = Math.max(0, promedioPl);
    for (RangoEquipo r : values()) {
      if (pl >= r.minInclusive && pl <= r.maxInclusive) {
        return r;
      }
    }
    return PRINCIPIANTE;
  }

  public static String assetPath(RangoEquipo rango) {
    // Los archivos viven en /resources/core/images/rangos-equipo/ y se sirven via /images/**
    return "/images/rangos-equipo/" + rango.iconoAsset;
  }
}
