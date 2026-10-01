package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Arbitro;
import com.tallerwebi.dominio.Cruce;
import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.FilaPosicion;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioInscripcionLiga;
import com.tallerwebi.dominio.RepositorioLiga;
import com.tallerwebi.dominio.RepositorioPartidoLiga;
import com.tallerwebi.dominio.ResultadoInscripcion;
import com.tallerwebi.dominio.RoundRobin;
import com.tallerwebi.dominio.ServicioArbitro;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.ServicioLiga;
import com.tallerwebi.dominio.Usuario;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioLiga")
@Transactional
public class ServicioLigaImpl implements ServicioLiga {

  private static final int MINIMO_EQUIPOS = 2;

  private final RepositorioLiga repoLiga;
  private final RepositorioInscripcionLiga repoInscripcion;
  private final RepositorioPartidoLiga repoPartido;
  private final ServicioEquipo servicioEquipo;
  private final ServicioArbitro servicioArbitro;

  @Autowired
  public ServicioLigaImpl(
    RepositorioLiga repoLiga,
    RepositorioInscripcionLiga repoInscripcion,
    RepositorioPartidoLiga repoPartido,
    ServicioEquipo servicioEquipo,
    ServicioArbitro servicioArbitro
  ) {
    this.repoLiga = repoLiga;
    this.repoInscripcion = repoInscripcion;
    this.repoPartido = repoPartido;
    this.servicioEquipo = servicioEquipo;
    this.servicioArbitro = servicioArbitro;
  }

  @Override
  public Liga buscarPorId(Long ligaId) {
    return ligaId == null ? null : repoLiga.buscarPorId(ligaId);
  }

  @Override
  public ResultadoInscripcion inscribir(Usuario usuario, Long ligaId) {
    if (usuario == null || usuario.getId() == null || ligaId == null) {
      return ResultadoInscripcion.INVALIDO;
    }
    Liga liga = repoLiga.buscarPorId(ligaId);
    ResultadoInscripcion validacion = validar(usuario, liga);
    if (validacion != null) {
      return validacion;
    }
    List<Equipo> equipos = servicioEquipo.listarEquiposActivosDe(usuario);
    if (equipos == null || equipos.isEmpty()) {
      return ResultadoInscripcion.SIN_EQUIPO_ACTIVO;
    }
    InscripcionLiga insc = new InscripcionLiga();
    insc.setLiga(liga);
    insc.setUsuario(usuario);
    insc.setEquipo(equipos.get(0));
    insc.setNombreEquipo(equipos.get(0).getNombre());
    repoInscripcion.guardar(insc);
    return ResultadoInscripcion.INSCRIPTO;
  }

  private ResultadoInscripcion validar(Usuario usuario, Liga liga) {
    if (liga == null) {
      return ResultadoInscripcion.INVALIDO;
    }
    if (
      !Boolean.TRUE.equals(liga.getInscripcionAbierta()) ||
      !repoPartido.listarPorLiga(liga.getId()).isEmpty()
    ) {
      return ResultadoInscripcion.INSCRIPCION_CERRADA;
    }
    if (repoInscripcion.existePara(usuario.getId(), liga.getId())) {
      return ResultadoInscripcion.YA_INSCRIPTO;
    }
    Integer cupo = liga.getCupoEquipos();
    if (cupo != null && repoInscripcion.listarPorLiga(liga.getId()).size() >= cupo) {
      return ResultadoInscripcion.CUPO_COMPLETO;
    }
    return null;
  }

  @Override
  public boolean yaInscripto(Usuario usuario, Long ligaId) {
    if (usuario == null || usuario.getId() == null || ligaId == null) {
      return false;
    }
    return repoInscripcion.existePara(usuario.getId(), ligaId);
  }

  @Override
  public List<InscripcionLiga> listarInscripciones(Long ligaId) {
    return ligaId == null ? Collections.emptyList() : repoInscripcion.listarPorLiga(ligaId);
  }

  @Override
  public boolean generarFixtureSiNoExiste(Long ligaId) {
    if (ligaId == null || !repoPartido.listarPorLiga(ligaId).isEmpty()) {
      return false;
    }
    Liga liga = repoLiga.buscarPorId(ligaId);
    List<InscripcionLiga> equipos = repoInscripcion.listarPorLiga(ligaId);
    if (liga == null || equipos.size() < MINIMO_EQUIPOS) {
      return false;
    }
    List<Arbitro> arbitros = servicioArbitro.listarRanking();
    List<List<Cruce<InscripcionLiga>>> fechas = RoundRobin.idaYVuelta(equipos);
    int designados = 0;
    for (int numero = 0; numero < fechas.size(); numero++) {
      for (Cruce<InscripcionLiga> cruce : fechas.get(numero)) {
        PartidoLiga partido = new PartidoLiga();
        partido.setLiga(liga);
        partido.setFecha(numero + 1);
        partido.setLocal(cruce.getLocal());
        partido.setVisitante(cruce.getVisitante());
        if (!arbitros.isEmpty()) {
          partido.setArbitro(arbitros.get(designados % arbitros.size()));
        }
        designados++;
        repoPartido.guardar(partido);
      }
    }
    return true;
  }

  @Override
  public Map<Integer, List<PartidoLiga>> obtenerFechas(Long ligaId) {
    Map<Integer, List<PartidoLiga>> fechas = new TreeMap<>();
    if (ligaId == null) {
      return fechas;
    }
    for (PartidoLiga partido : repoPartido.listarPorLiga(ligaId)) {
      fechas.computeIfAbsent(partido.getFecha(), f -> new ArrayList<>()).add(partido);
    }
    return fechas;
  }

  @Override
  public boolean registrarResultado(Long partidoId, Integer golesLocal, Integer golesVisitante) {
    if (partidoId == null || golesLocal == null || golesVisitante == null) {
      return false;
    }
    if (golesLocal < 0 || golesVisitante < 0) {
      return false;
    }
    PartidoLiga partido = repoPartido.buscarPorId(partidoId);
    if (partido == null || partido.estaJugado()) {
      return false;
    }
    partido.setGolesLocal(golesLocal);
    partido.setGolesVisitante(golesVisitante);
    partido.setEstado(PartidoLiga.JUGADO);
    repoPartido.actualizar(partido);
    return true;
  }

  @Override
  public List<FilaPosicion> calcularTabla(Long ligaId) {
    if (ligaId == null) {
      return Collections.emptyList();
    }
    Map<Long, FilaPosicion> filas = new LinkedHashMap<>();
    for (InscripcionLiga insc : repoInscripcion.listarPorLiga(ligaId)) {
      filas.put(insc.getId(), new FilaPosicion(insc.getId(), insc.getNombreVisible()));
    }
    for (PartidoLiga partido : repoPartido.listarPorLiga(ligaId)) {
      if (!partido.estaJugado()) {
        continue;
      }
      FilaPosicion local = filas.get(partido.getLocal().getId());
      FilaPosicion visitante = filas.get(partido.getVisitante().getId());
      if (local != null && visitante != null) {
        local.registrar(partido.getGolesLocal(), partido.getGolesVisitante());
        visitante.registrar(partido.getGolesVisitante(), partido.getGolesLocal());
      }
    }
    List<FilaPosicion> tabla = new ArrayList<>(filas.values());
    tabla.sort(FilaPosicion.ORDEN);
    return tabla;
  }

  @Override
  public List<Equipo> listarEquiposInscriptos(Long ligaId) {
    return ligaId == null ? Collections.emptyList() : repoInscripcion.listarEquipos(ligaId);
  }
}
