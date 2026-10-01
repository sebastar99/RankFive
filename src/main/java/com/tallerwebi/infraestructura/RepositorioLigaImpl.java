package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.RepositorioLiga;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioLiga")
public class RepositorioLigaImpl implements RepositorioLiga {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioLigaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Liga liga) {
    sessionFactory.getCurrentSession().persist(liga);
  }

  @Override
  public Liga buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Liga.class, id);
  }

  @Override
  public List<Liga> listarTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Liga l order by l.fechaInicio desc", Liga.class)
      .list();
  }
}
