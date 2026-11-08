package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadisticaEquipo;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioEncuentroTorneo;
import com.tallerwebi.dominio.RepositorioInscripcionLiga;
import com.tallerwebi.dominio.RepositorioInscripcionTorneo;
import com.tallerwebi.dominio.RepositorioPartidoLiga;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.ServicioEstadisticasEquipo;
import com.tallerwebi.dominio.Usuario;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioEstadisticasEquipo")
@Transactional(readOnly = true)
public class ServicioEstadisticasEquipoImpl implements ServicioEstadisticasEquipo {

  private final ServicioEquipo servicioEquipo;
  private final RepositorioPartidoLiga repoPartidoLiga;
  private final RepositorioEncuentroTorneo repoEncuentro;
  private final RepositorioInscripcionLiga repoInscripcionLiga;
  private final RepositorioInscripcionTorneo repoInscripcionTorneo;

  @Autowired
  public ServicioEstadisticasEquipoImpl(
    ServicioEquipo servicioEquipo,
    RepositorioPartidoLiga repoPartidoLiga,
    RepositorioEncuentroTorneo repoEncuentro,
    RepositorioInscripcionLiga repoInscripcionLiga,
    RepositorioInscripcionTorneo repoInscripcionTorneo
  ) {
    this.servicioEquipo = servicioEquipo;
    this.repoPartidoLiga = repoPartidoLiga;
    this.repoEncuentro = repoEncuentro;
    this.repoInscripcionLiga = repoInscripcionLiga;
    this.repoInscripcionTorneo = repoInscripcionTorneo;
  }

  @Override
  public List<EstadisticaEquipo> estadisticasDelUsuario(Usuario usuario) {
    if (usuario == null || usuario.getId() == null) {
      return Collections.emptyList();
    }
    Map<Long, EstadisticaEquipo> porEquipo = new LinkedHashMap<>();
    for (Equipo equipo : servicioEquipo.listarEquiposActivosDe(usuario)) {
      porEquipo.put(equipo.getId(), new EstadisticaEquipo(equipo));
    }
    if (porEquipo.isEmpty()) {
      return Collections.emptyList();
    }
    sumarPartidosDeLiga(porEquipo);
    sumarEncuentrosDeTorneo(porEquipo);
    sumarCompetencias(porEquipo);
    return porEquipo.values().stream().sorted(EstadisticaEquipo.ORDEN).toList();
  }

  private void sumarPartidosDeLiga(Map<Long, EstadisticaEquipo> porEquipo) {
    for (PartidoLiga partido : repoPartidoLiga.listarJugados()) {
      registrar(
        porEquipo,
        partido.getLocal().getEquipo(),
        partido.getGolesLocal(),
        partido.getGolesVisitante()
      );
      registrar(
        porEquipo,
        partido.getVisitante().getEquipo(),
        partido.getGolesVisitante(),
        partido.getGolesLocal()
      );
    }
  }

  private void sumarEncuentrosDeTorneo(Map<Long, EstadisticaEquipo> porEquipo) {
    for (EncuentroTorneo encuentro : repoEncuentro.listarJugados()) {
      if (encuentro.getParticipanteA() == null || encuentro.getParticipanteB() == null) {
        continue;
      }
      registrar(
        porEquipo,
        encuentro.getParticipanteA().getEquipo(),
        encuentro.getGolesA(),
        encuentro.getGolesB()
      );
      registrar(
        porEquipo,
        encuentro.getParticipanteB().getEquipo(),
        encuentro.getGolesB(),
        encuentro.getGolesA()
      );
    }
  }

  private void sumarCompetencias(Map<Long, EstadisticaEquipo> porEquipo) {
    for (InscripcionLiga inscripcion : repoInscripcionLiga.listarTodas()) {
      sumarCompetencia(porEquipo, inscripcion.getEquipo());
    }
    for (InscripcionTorneo inscripcion : repoInscripcionTorneo.listarTodas()) {
      sumarCompetencia(porEquipo, inscripcion.getEquipo());
    }
  }

  private static void sumarCompetencia(Map<Long, EstadisticaEquipo> porEquipo, Equipo equipo) {
    EstadisticaEquipo estadistica = equipo == null ? null : porEquipo.get(equipo.getId());
    if (estadistica != null) {
      estadistica.sumarCompetencia();
    }
  }

  private static void registrar(
    Map<Long, EstadisticaEquipo> porEquipo,
    Equipo equipo,
    Integer propios,
    Integer rivales
  ) {
    EstadisticaEquipo estadistica = equipo == null ? null : porEquipo.get(equipo.getId());
    if (estadistica != null && propios != null && rivales != null) {
      estadistica.registrar(propios, rivales);
    }
  }
}
