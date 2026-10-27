package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Mensaje;
import com.tallerwebi.dominio.RepositorioMensaje;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioMensajeImpl implements RepositorioMensaje {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioMensajeImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Mensaje mensaje) {
    sessionFactory.getCurrentSession().persist(mensaje);
  }

  @Override
  public List<Mensaje> buscarConversacion(Long usuarioId, Long otroUsuarioId) {
    String consulta =
      "SELECT m FROM Mensaje m " +
      "JOIN FETCH m.emisor " +
      "JOIN FETCH m.destinatario " +
      "WHERE (m.emisor.id = :usuarioId " +
      "AND m.destinatario.id = :otroUsuarioId) " +
      "OR (m.emisor.id = :otroUsuarioId " +
      "AND m.destinatario.id = :usuarioId) " +
      "ORDER BY m.fechaEnvio ASC, m.id ASC";

    return sessionFactory
      .getCurrentSession()
      .createQuery(consulta, Mensaje.class)
      .setParameter("usuarioId", usuarioId)
      .setParameter("otroUsuarioId", otroUsuarioId)
      .getResultList();
  }
}
