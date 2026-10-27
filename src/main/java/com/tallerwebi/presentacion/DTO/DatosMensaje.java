package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Mensaje;
import lombok.Getter;

@Getter
public class DatosMensaje {

  private final Long id;
  private final Long emisorId;
  private final Long destinatarioId;
  private final String contenido;
  private final String fechaEnvio;

  public DatosMensaje(Mensaje mensaje) {
    this.id = mensaje.getId();
    this.emisorId = mensaje.getEmisor().getId();
    this.destinatarioId = mensaje.getDestinatario().getId();
    this.contenido = mensaje.getContenido();
    this.fechaEnvio = mensaje.getFechaEnvio().toString();
  }
}
