package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadoEquipo;
import com.tallerwebi.dominio.RepositorioEquipo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioEquipo")
public class RepositorioEquipoImpl implements RepositorioEquipo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioEquipoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Equipo equipo) {
    sessionFactory.getCurrentSession().persist(equipo);
  }

  @Override
  public void actualizar(Equipo equipo) {
    sessionFactory.getCurrentSession().merge(equipo);
  }

  @Override
  public Equipo buscarPorId(Long id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct e from Equipo e join fetch e.owner left join fetch e.jugadores where e.id = :id",
        Equipo.class
      )
      .setParameter("id", id)
      .uniqueResult();
  }

  @Override
  public List<Equipo> listarActivosDe(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct e from Equipo e join fetch e.owner left join fetch e.jugadores " +
        "where e.estado = :activo and exists (" +
        "select 1 from Equipo e2 join e2.jugadores j where e2.id = e.id and j.id = :u) " +
        "order by e.fechaCreacion desc",
        Equipo.class
      )
      .setParameter("activo", EstadoEquipo.ACTIVO)
      .setParameter("u", usuarioId)
      .list();
  }
}
