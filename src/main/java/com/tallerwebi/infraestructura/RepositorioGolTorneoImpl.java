package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.GolTorneo;
import com.tallerwebi.dominio.RepositorioGolTorneo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioGolTorneo")
public class RepositorioGolTorneoImpl implements RepositorioGolTorneo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioGolTorneoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(GolTorneo gol) {
    sessionFactory.getCurrentSession().persist(gol);
  }

  @Override
  public List<GolTorneo> listarPorTorneo(Long torneoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select g from GolTorneo g join fetch g.encuentro e join e.fase f " +
        "join fetch g.inscripcion i left join fetch i.equipo " +
        "where f.torneo.id = :t order by g.id asc",
        GolTorneo.class
      )
      .setParameter("t", torneoId)
      .list();
  }

  @Override
  public long contarPor(Long encuentroId, Long inscripcionId) {
    Long total = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(g) from GolTorneo g where g.encuentro.id = :e and g.inscripcion.id = :i",
        Long.class
      )
      .setParameter("e", encuentroId)
      .setParameter("i", inscripcionId)
      .uniqueResult();
    return total == null ? 0L : total;
  }
}
