package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.RepositorioEncuentroTorneo;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioEncuentroTorneo")
public class RepositorioEncuentroTorneoImpl implements RepositorioEncuentroTorneo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioEncuentroTorneoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public EncuentroTorneo buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(EncuentroTorneo.class, id);
  }

  @Override
  public void guardar(EncuentroTorneo encuentro) {
    sessionFactory.getCurrentSession().persist(encuentro);
  }

  @Override
  public void actualizar(EncuentroTorneo encuentro) {
    sessionFactory.getCurrentSession().merge(encuentro);
  }
}
