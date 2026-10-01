package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.RepositorioInscripcionTorneo;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioInscripcionTorneo")
public class RepositorioInscripcionTorneoImpl implements RepositorioInscripcionTorneo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioInscripcionTorneoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(InscripcionTorneo inscripcion) {
    sessionFactory.getCurrentSession().persist(inscripcion);
  }

  @Override
  public boolean existePara(Long usuarioId, Long torneoId) {
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(i.id) from InscripcionTorneo i where i.usuario.id = :u and i.torneo.id = :t",
        Long.class
      )
      .setParameter("u", usuarioId)
      .setParameter("t", torneoId)
      .uniqueResult();
    return count != null && count > 0;
  }

  @Override
  public java.util.List<InscripcionTorneo> listarPorTorneo(Long torneoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct i from InscripcionTorneo i join fetch i.usuario left join fetch i.equipo where i.torneo.id = :t order by i.fechaInscripcion asc",
        InscripcionTorneo.class
      )
      .setParameter("t", torneoId)
      .list();
  }

  @Override
  public java.util.List<Equipo> listarEquipos(Long torneoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct e from InscripcionTorneo i join i.equipo e left join fetch e.jugadores where i.torneo.id = :t order by e.nombre asc",
        Equipo.class
      )
      .setParameter("t", torneoId)
      .list();
  }
}
