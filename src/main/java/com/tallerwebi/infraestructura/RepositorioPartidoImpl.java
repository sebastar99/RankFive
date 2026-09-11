package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Partido;
import com.tallerwebi.dominio.RepositorioPartido;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioPartido")
public class RepositorioPartidoImpl implements RepositorioPartido {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioPartidoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Partido partido) {
    sessionFactory.getCurrentSession().persist(partido);
  }

  @Override
  public void actualizar(Partido partido) {
    sessionFactory.getCurrentSession().merge(partido);
  }

  @Override
  public Partido buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Partido.class, id);
  }

  @Override
  public List<Partido> listarPartidosDe(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct p from Partido p " +
        "left join fetch p.equipoA a left join fetch p.equipoB b " +
        "where a.id = :u or b.id = :u order by p.fecha desc",
        Partido.class
      )
      .setParameter("u", usuarioId)
      .list();
  }
}
