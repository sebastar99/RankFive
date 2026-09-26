package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.RepositorioInscripcionLiga;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioInscripcionLiga")
public class RepositorioInscripcionLigaImpl implements RepositorioInscripcionLiga {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioInscripcionLigaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(InscripcionLiga inscripcion) {
    sessionFactory.getCurrentSession().persist(inscripcion);
  }

  @Override
  public boolean existePara(Long usuarioId, Long ligaId) {
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(i.id) from InscripcionLiga i where i.usuario.id = :u and i.liga.id = :l",
        Long.class
      )
      .setParameter("u", usuarioId)
      .setParameter("l", ligaId)
      .uniqueResult();
    return count != null && count > 0;
  }

  @Override
  public List<InscripcionLiga> listarPorLiga(Long ligaId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct i from InscripcionLiga i join fetch i.usuario left join fetch i.equipo where i.liga.id = :l order by i.fechaInscripcion asc, i.id asc",
        InscripcionLiga.class
      )
      .setParameter("l", ligaId)
      .list();
  }

  @Override
  public List<Equipo> listarEquipos(Long ligaId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct e from InscripcionLiga i join i.equipo e left join fetch e.jugadores where i.liga.id = :l order by e.nombre asc",
        Equipo.class
      )
      .setParameter("l", ligaId)
      .list();
  }
}
