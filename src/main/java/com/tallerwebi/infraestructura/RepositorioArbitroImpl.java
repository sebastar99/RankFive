package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Arbitro;
import com.tallerwebi.dominio.CalificacionArbitro;
import com.tallerwebi.dominio.RepositorioArbitro;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioArbitro")
public class RepositorioArbitroImpl implements RepositorioArbitro {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioArbitroImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Arbitro arbitro) {
    sessionFactory.getCurrentSession().persist(arbitro);
  }

  @Override
  public void actualizar(Arbitro arbitro) {
    sessionFactory.getCurrentSession().merge(arbitro);
  }

  @Override
  public Arbitro buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Arbitro.class, id);
  }

  @Override
  public List<Arbitro> listarTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Arbitro a order by a.nombre asc", Arbitro.class)
      .list();
  }

  @Override
  public void guardarCalificacion(CalificacionArbitro calificacion) {
    sessionFactory.getCurrentSession().persist(calificacion);
  }

  @Override
  public boolean existeCalificacion(Long usuarioId, Long encuentroId) {
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(c.id) from CalificacionArbitro c where c.usuario.id = :u and c.encuentro.id = :e",
        Long.class
      )
      .setParameter("u", usuarioId)
      .setParameter("e", encuentroId)
      .uniqueResult();
    return count != null && count > 0;
  }
}
