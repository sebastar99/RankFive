package com.tallerwebi.dominio.implementacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.ImagenGenerada;
import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioPartido;
import com.tallerwebi.dominio.ServicioGemini;
import com.tallerwebi.dominio.ServicioIA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioIAImpl implements ServicioIA {

  private final RepositorioPartido repositorioPartido;

  private final ServicioGemini servicioGemini;

  @Autowired
  public ServicioIAImpl(RepositorioPartido repositorioPartido, ServicioGemini servicioGemini) {
    this.repositorioPartido = repositorioPartido;
    this.servicioGemini = servicioGemini;
  }

  @Override
  @Transactional
  public String generarPreviaPartido(Long partidoId) throws JsonProcessingException {
    return servicioGemini.generarTexto(construirPrompt(buscarPartido(partidoId)));
  }

  @Override
  @Transactional
  public ImagenGenerada generarBannerPartido(Long partidoId) throws JsonProcessingException {
    return servicioGemini.generarImagen(construirPromptBanner(buscarPartido(partidoId)));
  }

  private Partido buscarPartido(Long partidoId) {
    Partido partido = repositorioPartido.buscarPorId(partidoId);
    if (partido == null) {
      throw new IllegalArgumentException("No existe el partido con id: " + partidoId);
    }
    return partido;
  }

  private String construirPrompt(Partido partido) {
    StringBuilder prompt = new StringBuilder();

    prompt.append(
      """
      Actuá como periodista deportivo especializado
      en fútbol.

      Generá una previa periodística del partido
      utilizando exclusivamente los datos proporcionados.

      IMPORTANTE:
      - No inventes información.
      - No inventes jugadores.
      - No inventes estadísticas.
      - No inventes resultados.
      - No inventes posiciones en una tabla.
      - Si un dato no está disponible, no lo menciones.
      - El análisis debe basarse únicamente en los datos
        recibidos.

      DATOS DEL PARTIDO

      """
    );

    prompt.append("ID del partido: ").append(partido.getId()).append("\n");

    prompt.append("Fecha: ").append(partido.getFecha()).append("\n");

    prompt.append("\nEQUIPO A\n");

    prompt.append("Jugadores:\n");

    partido
      .getEquipoA()
      .forEach(usuario -> {
        prompt.append("- ").append(usuario.getPerfil().getNombreCompleto()).append("\n");
      });

    prompt.append("\nEQUIPO B\n");

    prompt.append("Jugadores:\n");

    partido
      .getEquipoB()
      .forEach(usuario -> {
        prompt.append("- ").append(usuario.getPerfil().getNombreCompleto()).append("\n");
      });

    prompt.append(
      """

      FORMATO DE RESPUESTA

      PREVIA DEL PARTIDO

      Escribí una introducción breve sobre el
      enfrentamiento.

      EQUIPO A

      Analizá brevemente el equipo utilizando
      únicamente la información disponible.

      EQUIPO B

      Analizá brevemente el equipo utilizando
      únicamente la información disponible.

      JUGADORES DESTACADOS

      Mencioná los jugadores que puedan destacarse
      según los datos proporcionados.

      CLAVE DEL PARTIDO

      Explicá qué aspecto del partido podría ser
      determinante.

      FAVORITO SEGÚN LOS DATOS

      Si los datos permiten determinar un equipo
      con ventaja, indicá cuál y explicá brevemente
      por qué.

      No inventes un resultado ni un marcador.
      """
    );

    return prompt.toString();
  }

  private String construirPromptBanner(Partido partido) {
    return """
    Creá un banner panorámico horizontal para la previa de un partido de fútbol amateur.
    Mostrá dos equipos de cinco jugadores a punto de disputar un encuentro nocturno, con un
    campo iluminado, energía competitiva y una estética deportiva moderna y cinematográfica.
    Dejá espacio visual limpio en el centro para superponer datos en el futuro.
    No incluyas palabras, letras, números, escudos, logos, marcadores ni marcas de agua.
    No uses uniformes de equipos reales ni rostros reconocibles.

    Equipo A: %s
    Equipo B: %s
    """.formatted(nombres(partido.getEquipoA()), nombres(partido.getEquipoB()));
  }

  private String nombres(java.util.Collection<com.tallerwebi.dominio.Usuario> jugadores) {
    return jugadores
      .stream()
      .map(jugador -> jugador.getPerfil().getNombreCompleto())
      .collect(java.util.stream.Collectors.joining(", "));
  }
}
