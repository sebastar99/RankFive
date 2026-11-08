package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.GolLiga;
import com.tallerwebi.dominio.RepositorioGolLiga;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioGolLiga")
public class RepositorioGolLigaImpl implements RepositorioGolLiga {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioGolLigaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(GolLiga gol) {
    sessionFactory.getCurrentSession().persist(gol);
  }

  @Override
  public List<GolLiga> listarPorLiga(Long ligaId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select g from GolLiga g " +
        "join fetch g.partido p " +
        "join fetch g.inscripcion i left join fetch i.equipo " +
        "where p.liga.id = :l order by g.id asc",
        GolLiga.class
      )
      .setParameter("l", ligaId)
      .list();
  }

  @Override
  public void borrarPorPartido(Long id) {
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("delete from GolLiga g where g.partido.id = :id")
      .setParameter("id", id)
      .executeUpdate();
  }

  @Override
  public long contarGolesDe(Long usuarioId) {
    return contar("select count(g) from GolLiga g where g.usuarioGoleador.id = :u", usuarioId);
  }

  @Override
  public long contarAsistenciasDe(Long usuarioId) {
    return contar("select count(g) from GolLiga g where g.usuarioAsistente.id = :u", usuarioId);
  }

  private long contar(String consulta, Long usuarioId) {
    Long total = sessionFactory
      .getCurrentSession()
      .createQuery(consulta, Long.class)
      .setParameter("u", usuarioId)
      .uniqueResult();
    return total == null ? 0L : total;
  }
}
