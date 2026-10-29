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
  private final String emisorNombre;

  public DatosMensaje(Mensaje mensaje) {
    this(mensaje, "Usuario #" + mensaje.getEmisor().getId());
  }

  public DatosMensaje(Mensaje mensaje, String emisorNombre) {
    this.id = mensaje.getId();
    this.emisorId = mensaje.getEmisor().getId();
    this.destinatarioId = mensaje.getDestinatario().getId();
    this.contenido = mensaje.getContenido();
    this.fechaEnvio = mensaje.getFechaEnvio().toString();
    this.emisorNombre = emisorNombre;
  }
}
