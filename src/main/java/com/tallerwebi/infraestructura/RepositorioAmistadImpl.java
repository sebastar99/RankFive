package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Amistad;
import com.tallerwebi.dominio.RepositorioAmistad;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioAmistad")
public class RepositorioAmistadImpl implements RepositorioAmistad {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioAmistadImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Amistad amistad) {
    sessionFactory.getCurrentSession().persist(amistad);
  }

  @Override
  public boolean existeRelacion(Long usuarioId, Long amigoId) {
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(a.id) from Amistad a where a.usuario.id = :u and a.amigo.id = :a",
        Long.class
      )
      .setParameter("u", usuarioId)
      .setParameter("a", amigoId)
      .uniqueResult();
    return count != null && count > 0;
  }

  @Override
  public List<Usuario> listarAmigosDe(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select a.amigo from Amistad a where a.usuario.id = :u order by a.amigo.perfil.pl desc",
        Usuario.class
      )
      .setParameter("u", usuarioId)
      .list();
  }
}
