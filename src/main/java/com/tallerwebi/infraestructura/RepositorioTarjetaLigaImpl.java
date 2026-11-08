package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioTarjetaLiga;
import com.tallerwebi.dominio.TarjetaLiga;
import com.tallerwebi.dominio.enums.TipoTarjeta;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTarjetaLiga")
public class RepositorioTarjetaLigaImpl implements RepositorioTarjetaLiga {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioTarjetaLigaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(TarjetaLiga tarjeta) {
    sessionFactory.getCurrentSession().persist(tarjeta);
  }

  @Override
  public void borrarPorPartido(Long partidoId) {
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("delete from TarjetaLiga t where t.partido.id = :p")
      .setParameter("p", partidoId)
      .executeUpdate();
  }

  @Override
  public List<TarjetaLiga> listarPorLiga(Long ligaId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select t from TarjetaLiga t join fetch t.partido p " +
        "join fetch t.inscripcion i left join fetch i.equipo " +
        "where p.liga.id = :l order by t.id asc",
        TarjetaLiga.class
      )
      .setParameter("l", ligaId)
      .list();
  }

  @Override
  public long contarDe(Long usuarioId, TipoTarjeta tipo) {
    Long total = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(t) from TarjetaLiga t where t.usuarioAmonestado.id = :u and t.tipo = :tipo",
        Long.class
      )
      .setParameter("u", usuarioId)
      .setParameter("tipo", tipo)
      .uniqueResult();
    return total == null ? 0L : total;
  }
}
