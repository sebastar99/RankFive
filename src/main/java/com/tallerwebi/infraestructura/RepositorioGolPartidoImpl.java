package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.GolPartido;
import com.tallerwebi.dominio.RepositorioGolPartido;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioGolPartido")
public class RepositorioGolPartidoImpl implements RepositorioGolPartido {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioGolPartidoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(GolPartido gol) {
    sessionFactory.getCurrentSession().persist(gol);
  }

  @Override
  public void borrarPorPartido(Long partidoId) {
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("delete from GolPartido g where g.partido.id = :p")
      .setParameter("p", partidoId)
      .executeUpdate();
  }

  @Override
  public List<GolPartido> listarPorPartido(Long partidoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select g from GolPartido g join fetch g.partido p where p.id = :p order by g.id asc",
        GolPartido.class
      )
      .setParameter("p", partidoId)
      .list();
  }
}
