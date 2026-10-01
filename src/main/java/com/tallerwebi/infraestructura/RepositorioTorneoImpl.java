package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioTorneo;
import com.tallerwebi.dominio.Torneo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTorneo")
public class RepositorioTorneoImpl implements RepositorioTorneo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioTorneoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Torneo torneo) {
    sessionFactory.getCurrentSession().persist(torneo);
  }

  @Override
  public Torneo buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Torneo.class, id);
  }

  @Override
  public List<Torneo> listarTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Torneo t order by t.fechaInicio desc", Torneo.class)
      .list();
  }
}
