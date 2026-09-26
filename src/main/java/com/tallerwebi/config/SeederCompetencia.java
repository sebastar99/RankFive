package com.tallerwebi.config;

import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.RepositorioLiga;
import com.tallerwebi.dominio.RepositorioTorneo;
import com.tallerwebi.dominio.Torneo;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeederCompetencia {

  private final RepositorioTorneo repoTorneo;
  private final RepositorioLiga repoLiga;

  @Autowired
  public SeederCompetencia(RepositorioTorneo repoTorneo, RepositorioLiga repoLiga) {
    this.repoTorneo = repoTorneo;
    this.repoLiga = repoLiga;
  }

  @EventListener(ContextRefreshedEvent.class)
  @Transactional
  public void seed() {
    // Semillas básicas si no hay datos
    if (repoTorneo.listarTodos().isEmpty()) {
      Torneo apertura = new Torneo();
      apertura.setNombre("Copa Rank5 · Apertura");
      apertura.setDescripcion("Copa a doble eliminación con premios PL");
      apertura.setFormato(5);
      apertura.setCupoEquipos(16);
      apertura.setInscripcionAbierta(Boolean.TRUE);
      apertura.setFechaInicio(LocalDate.now().plusDays(14));
      apertura.setUbicacion("CABA");
      repoTorneo.guardar(apertura);

      Torneo relampago = new Torneo();
      relampago.setNombre("Torneo Relámpago");
      relampago.setDescripcion("Jornada única · Eliminación directa");
      relampago.setFormato(5);
      relampago.setCupoEquipos(12);
      relampago.setInscripcionAbierta(Boolean.FALSE);
      relampago.setFechaInicio(LocalDate.now().plusDays(21));
      relampago.setUbicacion("Lanús");
      repoTorneo.guardar(relampago);
    }

    if (repoLiga.listarTodos().isEmpty()) {
      Liga nocturna = new Liga();
      nocturna.setNombre("Liga Nocturna");
      nocturna.setDescripcion("Fechas entre semana por la noche");
      nocturna.setFormato(7);
      nocturna.setCupoEquipos(8);
      nocturna.setInscripcionAbierta(Boolean.TRUE);
      nocturna.setFechaInicio(LocalDate.now().plusDays(10));
      nocturna.setUbicacion("Villa Crespo");
      repoLiga.guardar(nocturna);

      Liga verano = new Liga();
      verano.setNombre("Liga de Verano");
      verano.setDescripcion("Temporada corta en vacaciones");
      verano.setFormato(8);
      verano.setCupoEquipos(10);
      verano.setInscripcionAbierta(Boolean.FALSE);
      verano.setFechaInicio(LocalDate.now().plusMonths(3));
      verano.setUbicacion("Mar del Plata");
      repoLiga.guardar(verano);
    }
  }
}
