package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.FaseTorneo;
import com.tallerwebi.dominio.RepositorioFaseTorneo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioFaseTorneo")
public class RepositorioFaseTorneoImpl implements RepositorioFaseTorneo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioFaseTorneoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(FaseTorneo fase) {
    sessionFactory.getCurrentSession().persist(fase);
  }

  @Override
  public List<FaseTorneo> listarPorTorneo(Long torneoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct f from FaseTorneo f left join fetch f.encuentros e " +
        "left join fetch e.participanteA pa left join fetch pa.usuario left join fetch pa.equipo " +
        "left join fetch e.participanteB pb left join fetch pb.usuario left join fetch pb.equipo " +
        "left join fetch e.arbitro " +
        "where f.torneo.id = :t order by f.numero asc, e.id asc",
        FaseTorneo.class
      )
      .setParameter("t", torneoId)
      .list();
  }
}
