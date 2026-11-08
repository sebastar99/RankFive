package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioTarjetaTorneo;
import com.tallerwebi.dominio.TarjetaTorneo;
import com.tallerwebi.dominio.enums.TipoTarjeta;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTarjetaTorneo")
public class RepositorioTarjetaTorneoImpl implements RepositorioTarjetaTorneo {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioTarjetaTorneoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(TarjetaTorneo tarjeta) {
    sessionFactory.getCurrentSession().persist(tarjeta);
  }

  @Override
  public void borrarPorEncuentro(Long encuentroId) {
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("delete from TarjetaTorneo t where t.encuentro.id = :e")
      .setParameter("e", encuentroId)
      .executeUpdate();
  }

  @Override
  public List<TarjetaTorneo> listarPorTorneo(Long torneoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select t from TarjetaTorneo t join fetch t.encuentro e join e.fase f " +
        "join fetch t.inscripcion i left join fetch i.equipo " +
        "where f.torneo.id = :t order by t.id asc",
        TarjetaTorneo.class
      )
      .setParameter("t", torneoId)
      .list();
  }

  @Override
  public long contarDe(Long usuarioId, TipoTarjeta tipo) {
    Long total = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(t) from TarjetaTorneo t where t.usuarioAmonestado.id = :u and t.tipo = :tipo",
        Long.class
      )
      .setParameter("u", usuarioId)
      .setParameter("tipo", tipo)
      .uniqueResult();
    return total == null ? 0L : total;
  }
}
