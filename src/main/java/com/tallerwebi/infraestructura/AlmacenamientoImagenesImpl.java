package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.AlmacenamientoImagenes;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component("almacenamientoImagenes")
public class AlmacenamientoImagenesImpl implements AlmacenamientoImagenes {

  public static final String URL_PUBLICA = "/escudos/";

  private final Path directorio;

  public AlmacenamientoImagenesImpl() {
    this(directorioPorDefecto());
  }

  public AlmacenamientoImagenesImpl(Path directorio) {
    this.directorio = directorio;
  }

  public static Path directorioPorDefecto() {
    String configurado = System.getenv("RANKFIVE_ESCUDOS_DIR");
    if (configurado != null && !configurado.isBlank()) {
      return Paths.get(configurado);
    }
    return Paths.get(System.getProperty("user.home"), ".rankfive", "escudos");
  }

  @Override
  public String guardarEscudo(String nombreOriginal, byte[] contenido) {
    if (contenido == null || contenido.length == 0) {
      return null;
    }
    try {
      Files.createDirectories(directorio);
      String nombreArchivo = UUID.randomUUID() + extensionDe(nombreOriginal);
      Files.write(directorio.resolve(nombreArchivo), contenido);
      return URL_PUBLICA + nombreArchivo;
    } catch (IOException e) {
      throw new UncheckedIOException("No se pudo guardar el escudo del equipo", e);
    }
  }

  private static String extensionDe(String nombreOriginal) {
    if (nombreOriginal == null) {
      return ".png";
    }
    int punto = nombreOriginal.lastIndexOf('.');
    if (punto < 0 || punto == nombreOriginal.length() - 1) {
      return ".png";
    }
    String ext = nombreOriginal.substring(punto).toLowerCase(Locale.ROOT);
    return ext.matches("\\.(png|jpe?g|gif|webp)") ? ext : ".png";
  }
}
