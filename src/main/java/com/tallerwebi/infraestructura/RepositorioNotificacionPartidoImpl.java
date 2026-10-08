package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.NotificacionPartido;
import com.tallerwebi.dominio.RepositorioNotificacionPartido;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioNotificacionPartido")
public class RepositorioNotificacionPartidoImpl implements RepositorioNotificacionPartido {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioNotificacionPartidoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(NotificacionPartido notificacion) {
    sessionFactory.getCurrentSession().persist(notificacion);
  }

  @Override
  public List<NotificacionPartido> listarNoLeidasDe(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from NotificacionPartido n where n.usuario.id = :u and n.leida = false order by n.fecha desc",
        NotificacionPartido.class
      )
      .setParameter("u", usuarioId)
      .list();
  }

  @Override
  public void marcarLeidasDe(Long usuarioId) {
    sessionFactory
      .getCurrentSession()
      .createMutationQuery(
        "update NotificacionPartido n set n.leida = true where n.usuario.id = :u and n.leida = false"
      )
      .setParameter("u", usuarioId)
      .executeUpdate();
  }
}
