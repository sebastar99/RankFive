package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.NotificacionEncuentro;
import com.tallerwebi.dominio.RepositorioNotificacionEncuentro;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioNotificacionEncuentro")
public class RepositorioNotificacionEncuentroImpl implements RepositorioNotificacionEncuentro {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioNotificacionEncuentroImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(NotificacionEncuentro notificacion) {
    sessionFactory.getCurrentSession().persist(notificacion);
  }

  @Override
  public List<NotificacionEncuentro> listarNoLeidas(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from NotificacionEncuentro n where n.usuario.id = :u and n.leida = false order by n.fecha desc",
        NotificacionEncuentro.class
      )
      .setParameter("u", usuarioId)
      .list();
  }
}
