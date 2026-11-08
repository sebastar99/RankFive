package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.EncuentroTorneo;
import com.tallerwebi.dominio.RepositorioEncuentroTorneo;
import java.util.List;
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

  @Override
  public List<EncuentroTorneo> listarJugados() {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select e from EncuentroTorneo e " +
        "left join fetch e.participanteA a left join fetch a.equipo " +
        "left join fetch e.participanteB b left join fetch b.equipo " +
        "where e.estado = 'JUGADO'",
        EncuentroTorneo.class
      )
      .list();
  }
}
