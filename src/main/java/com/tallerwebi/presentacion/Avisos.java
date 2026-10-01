package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ResultadoInscripcion;
import java.util.Map;

public final class Avisos {

  public static final String CALIFICADO = "CALIFICADO";
  public static final String CALIFICACION_INVALIDA = "CALIFICACION_INVALIDA";
  public static final String FIXTURE_GENERADO = "FIXTURE_GENERADO";
  public static final String FIXTURE_INVALIDO = "FIXTURE_INVALIDO";
  public static final String RESULTADO_GUARDADO = "RESULTADO_GUARDADO";
  public static final String RESULTADO_INVALIDO = "RESULTADO_INVALIDO";

  private static final String AVISO = "aviso";
  private static final String AVISO_OK = "avisoOk";

  private static final Map<String, String> OK = Map.of(
    CALIFICADO,
    "¡Gracias! Tu calificación al árbitro fue registrada",
    FIXTURE_GENERADO,
    "Fixture generado correctamente",
    RESULTADO_GUARDADO,
    "Resultado registrado"
  );

  private static final Map<String, String> ERROR = Map.of(
    CALIFICACION_INVALIDA,
    "No podés calificar a este árbitro (ya votaste o no jugaste el partido)",
    FIXTURE_INVALIDO,
    "No se pudo generar el fixture: se necesitan al menos 2 equipos inscriptos",
    RESULTADO_INVALIDO,
    "Resultado inválido (en eliminación directa no puede haber empate)"
  );

  private Avisos() {}

  public static void agregar(Map<String, Object> modelo, String codigo) {
    if (codigo == null || codigo.isBlank()) {
      return;
    }
    if (OK.containsKey(codigo)) {
      poner(modelo, OK.get(codigo), true);
      return;
    }
    if (ERROR.containsKey(codigo)) {
      poner(modelo, ERROR.get(codigo), false);
      return;
    }
    try {
      ResultadoInscripcion resultado = ResultadoInscripcion.valueOf(codigo);
      poner(modelo, resultado.getMensaje(), resultado.esExitoso());
    } catch (IllegalArgumentException ignored) {
      // codigo desconocido: no se muestra aviso
    }
  }

  private static void poner(Map<String, Object> modelo, String mensaje, boolean ok) {
    modelo.put(AVISO, mensaje);
    modelo.put(AVISO_OK, ok);
  }
}
