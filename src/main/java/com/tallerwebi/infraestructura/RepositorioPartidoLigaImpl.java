package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.PartidoLiga;
import com.tallerwebi.dominio.RepositorioPartidoLiga;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioPartidoLiga")
public class RepositorioPartidoLigaImpl implements RepositorioPartidoLiga {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioPartidoLigaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(PartidoLiga partido) {
    sessionFactory.getCurrentSession().persist(partido);
  }

  @Override
  public void actualizar(PartidoLiga partido) {
    sessionFactory.getCurrentSession().merge(partido);
  }

  @Override
  public PartidoLiga buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(PartidoLiga.class, id);
  }

  @Override
  public List<PartidoLiga> listarPorLiga(Long ligaId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select p from PartidoLiga p " +
        "join fetch p.local l left join fetch l.equipo " +
        "join fetch p.visitante v left join fetch v.equipo " +
        "left join fetch p.arbitro " +
        "where p.liga.id = :l order by p.fecha asc, p.id asc",
        PartidoLiga.class
      )
      .setParameter("l", ligaId)
      .list();
  }
}
