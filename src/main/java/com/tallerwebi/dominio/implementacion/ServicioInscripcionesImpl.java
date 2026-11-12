package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.RepositorioLiga;
import com.tallerwebi.dominio.RepositorioTorneo;
import com.tallerwebi.dominio.ServicioInscripciones;
import com.tallerwebi.dominio.Torneo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioInscripciones")
@Transactional
public class ServicioInscripcionesImpl implements ServicioInscripciones {

  private final RepositorioTorneo repoTorneo;
  private final RepositorioLiga repoLiga;

  @Autowired
  public ServicioInscripcionesImpl(RepositorioTorneo repoTorneo, RepositorioLiga repoLiga) {
    this.repoTorneo = repoTorneo;
    this.repoLiga = repoLiga;
  }

  @Override
  public boolean abrirInscripcionTorneo(Long torneoId) {
    Torneo torneo = torneoId == null ? null : repoTorneo.buscarPorId(torneoId);
    return torneo != null && torneo.abrirInscripcion();
  }

  @Override
  public boolean abrirInscripcionLiga(Long ligaId) {
    Liga liga = ligaId == null ? null : repoLiga.buscarPorId(ligaId);
    return liga != null && liga.abrirInscripcion();
  }
}
