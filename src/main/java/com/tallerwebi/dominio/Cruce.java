package com.tallerwebi.dominio;

public final class Cruce<T> {

  private final T local;
  private final T visitante;

  public Cruce(T local, T visitante) {
    this.local = local;
    this.visitante = visitante;
  }

  public T getLocal() {
    return local;
  }

  public T getVisitante() {
    return visitante;
  }

  public Cruce<T> invertido() {
    return new Cruce<>(visitante, local);
  }
}
