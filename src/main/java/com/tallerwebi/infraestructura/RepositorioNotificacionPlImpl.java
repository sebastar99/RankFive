package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.NotificacionPl;
import com.tallerwebi.dominio.RepositorioNotificacionPl;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioNotificacionPl")
public class RepositorioNotificacionPlImpl implements RepositorioNotificacionPl {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioNotificacionPlImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(NotificacionPl notificacion) {
    sessionFactory.getCurrentSession().persist(notificacion);
  }

  @Override
  public List<NotificacionPl> listarNoLeidasDe(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from NotificacionPl n where n.usuario.id = :u and n.leida = false order by n.fecha desc",
        NotificacionPl.class
      )
      .setParameter("u", usuarioId)
      .list();
  }

  @Override
  public void marcarLeidasDe(Long usuarioId) {
    sessionFactory
      .getCurrentSession()
      .createMutationQuery(
        "update NotificacionPl n set n.leida = true where n.usuario.id = :u and n.leida = false"
      )
      .setParameter("u", usuarioId)
      .executeUpdate();
  }
}
