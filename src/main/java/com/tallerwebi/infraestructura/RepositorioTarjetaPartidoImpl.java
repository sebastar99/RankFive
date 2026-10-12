package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioTarjetaPartido;
import com.tallerwebi.dominio.TarjetaPartido;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioTarjetaPartido")
public class RepositorioTarjetaPartidoImpl implements RepositorioTarjetaPartido {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioTarjetaPartidoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(TarjetaPartido tarjeta) {
    sessionFactory.getCurrentSession().persist(tarjeta);
  }

  @Override
  public void borrarPorPartido(Long partidoId) {
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("delete from TarjetaPartido g where g.partido.id = :p")
      .setParameter("p", partidoId)
      .executeUpdate();
  }

  @Override
  public List<TarjetaPartido> listarTarjetasPorPartido(Long partidoId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select g from TarjetaPartido g join fetch g.partido p where p.id = :p order by g.id asc",
        TarjetaPartido.class
      )
      .setParameter("p", partidoId)
      .list();
  }
}
