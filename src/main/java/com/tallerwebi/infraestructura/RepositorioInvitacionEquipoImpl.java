package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.EstadoInvitacionEquipo;
import com.tallerwebi.dominio.InvitacionEquipo;
import com.tallerwebi.dominio.RepositorioInvitacionEquipo;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioInvitacionEquipo")
public class RepositorioInvitacionEquipoImpl implements RepositorioInvitacionEquipo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioInvitacionEquipoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(InvitacionEquipo invitacion) {
    sessionFactory.getCurrentSession().persist(invitacion);
  }

  @Override
  public void actualizar(InvitacionEquipo invitacion) {
    sessionFactory.getCurrentSession().merge(invitacion);
  }

  @Override
  public InvitacionEquipo buscarPorId(Long id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct i from InvitacionEquipo i join fetch i.equipo e join fetch e.owner " +
        "left join fetch e.jugadores left join fetch i.equipoOrigen " +
        "join fetch i.usuario where i.id = :id",
        InvitacionEquipo.class
      )
      .setParameter("id", id)
      .uniqueResult();
  }

  @Override
  public List<InvitacionEquipo> listarDeEquipo(Long equipoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select i from InvitacionEquipo i join fetch i.usuario where i.equipo.id = :e",
        InvitacionEquipo.class
      )
      .setParameter("e", equipoId)
      .list();
  }

  @Override
  public List<InvitacionEquipo> listarPendientesPara(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select i from InvitacionEquipo i join fetch i.equipo e join fetch e.owner " +
        "left join fetch i.equipoOrigen " +
        "where i.usuario.id = :u and i.estado = :pendiente order by i.fecha desc",
        InvitacionEquipo.class
      )
      .setParameter("u", usuarioId)
      .setParameter("pendiente", EstadoInvitacionEquipo.PENDIENTE)
      .list();
  }

  @Override
  public List<InvitacionEquipo> listarRespuestasParaCapitan(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select i from InvitacionEquipo i join fetch i.usuario join fetch i.equipo e " +
        "where e.owner.id = :u and i.equipoOrigen is not null and i.respuestaLeida = false " +
        "and (i.estado <> :pendiente or i.respuestaPostergada = true) order by i.fecha desc",
        InvitacionEquipo.class
      )
      .setParameter("u", usuarioId)
      .setParameter("pendiente", EstadoInvitacionEquipo.PENDIENTE)
      .list();
  }

  @Override
  public long contarPendientesPara(Long usuarioId) {
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(i.id) from InvitacionEquipo i where i.usuario.id = :u and i.estado = :pendiente",
        Long.class
      )
      .setParameter("u", usuarioId)
      .setParameter("pendiente", EstadoInvitacionEquipo.PENDIENTE)
      .uniqueResult();
    return count != null ? count : 0L;
  }
}
