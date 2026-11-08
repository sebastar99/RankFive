package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.PosicionEquipo;
import com.tallerwebi.dominio.RepositorioEquipo;
import com.tallerwebi.dominio.ServicioRankingEquipos;
import com.tallerwebi.dominio.Usuario;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioRankingEquipos")
@Transactional(readOnly = true)
public class ServicioRankingEquiposImpl implements ServicioRankingEquipos {

  private static final Comparator<Equipo> ORDEN = Comparator
    .comparingInt(Equipo::getPromedioPl)
    .reversed()
    .thenComparing(Equipo::getNombre, String.CASE_INSENSITIVE_ORDER);

  private final RepositorioEquipo repositorioEquipo;

  @Autowired
  public ServicioRankingEquiposImpl(RepositorioEquipo repositorioEquipo) {
    this.repositorioEquipo = repositorioEquipo;
  }

  @Override
  public List<PosicionEquipo> obtenerRanking(Usuario usuario, int limite) {
    List<Equipo> ordenados = repositorioEquipo.listarActivos().stream().sorted(ORDEN).toList();
    List<PosicionEquipo> ranking = new ArrayList<>();
    for (int i = 0; i < ordenados.size(); i++) {
      boolean propio = esDe(usuario, ordenados.get(i));
      if (i < limite || propio) {
        ranking.add(new PosicionEquipo(i + 1, ordenados.get(i), propio));
      }
    }
    return ranking;
  }

  private static boolean esDe(Usuario usuario, Equipo equipo) {
    if (usuario == null || usuario.getId() == null) {
      return false;
    }
    return equipo.getJugadores().stream().anyMatch(j -> usuario.getId().equals(j.getId()));
  }
}
