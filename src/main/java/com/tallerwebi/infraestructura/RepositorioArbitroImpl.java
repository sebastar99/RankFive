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
    return contar("c.encuentro.id", usuarioId, encuentroId) > 0;
  }

  @Override
  public boolean existeCalificacionLiga(Long usuarioId, Long partidoLigaId) {
    return contar("c.partidoLiga.id", usuarioId, partidoLigaId) > 0;
  }

  @Override
  public List<Long> encuentrosCalificadosPor(Long usuarioId) {
    return idsCalificados("c.encuentro.id", usuarioId);
  }

  @Override
  public List<Long> partidosLigaCalificadosPor(Long usuarioId) {
    return idsCalificados("c.partidoLiga.id", usuarioId);
  }

  private long contar(String campo, Long usuarioId, Long partidoId) {
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(c.id) from CalificacionArbitro c where c.usuario.id = :u and " +
        campo +
        " = :p",
        Long.class
      )
      .setParameter("u", usuarioId)
      .setParameter("p", partidoId)
      .uniqueResult();
    return count == null ? 0L : count;
  }

  private List<Long> idsCalificados(String campo, Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select " +
        campo +
        " from CalificacionArbitro c where c.usuario.id = :u and " +
        campo +
        " is not null",
        Long.class
      )
      .setParameter("u", usuarioId)
      .list();
  }
}
