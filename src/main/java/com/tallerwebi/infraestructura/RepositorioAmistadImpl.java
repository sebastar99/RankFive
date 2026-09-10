package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Amistad;
import com.tallerwebi.dominio.EstadoAmistad;
import com.tallerwebi.dominio.RepositorioAmistad;
import com.tallerwebi.dominio.Usuario;
import java.util.Comparator;
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
  public void actualizar(Amistad amistad) {
    sessionFactory.getCurrentSession().merge(amistad);
  }

  @Override
  public Amistad buscarPorId(Long id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select a from Amistad a join fetch a.usuario join fetch a.amigo where a.id = :id",
        Amistad.class
      )
      .setParameter("id", id)
      .uniqueResult();
  }

  @Override
  public Amistad buscarRelacionEntre(Long unUsuarioId, Long otroUsuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select a from Amistad a where a.estado <> :rechazada " +
        "and ((a.usuario.id = :uno and a.amigo.id = :otro) " +
        "or (a.usuario.id = :otro and a.amigo.id = :uno))",
        Amistad.class
      )
      .setParameter("rechazada", EstadoAmistad.RECHAZADA)
      .setParameter("uno", unUsuarioId)
      .setParameter("otro", otroUsuarioId)
      .setMaxResults(1)
      .uniqueResult();
  }

  @Override
  public List<Usuario> listarAmigosDe(Long usuarioId) {
    List<Amistad> relaciones = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select a from Amistad a join fetch a.usuario join fetch a.amigo " +
        "where a.estado = :aceptada and (a.usuario.id = :u or a.amigo.id = :u)",
        Amistad.class
      )
      .setParameter("aceptada", EstadoAmistad.ACEPTADA)
      .setParameter("u", usuarioId)
      .list();

    return relaciones
      .stream()
      .map(relacion -> otroExtremoDe(relacion, usuarioId))
      .sorted(Comparator.comparingInt(RepositorioAmistadImpl::puntosDe).reversed())
      .toList();
  }

  @Override
  public List<Amistad> listarSolicitudesPendientesPara(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select a from Amistad a join fetch a.usuario join fetch a.amigo " +
        "where a.amigo.id = :u and a.estado = :pendiente order by a.fechaSolicitud desc",
        Amistad.class
      )
      .setParameter("u", usuarioId)
      .setParameter("pendiente", EstadoAmistad.PENDIENTE)
      .list();
  }

  @Override
  public long contarSolicitudesPendientesPara(Long usuarioId) {
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(a.id) from Amistad a where a.amigo.id = :u and a.estado = :pendiente",
        Long.class
      )
      .setParameter("u", usuarioId)
      .setParameter("pendiente", EstadoAmistad.PENDIENTE)
      .uniqueResult();
    return count != null ? count : 0L;
  }

  private Usuario otroExtremoDe(Amistad relacion, Long usuarioId) {
    return usuarioId.equals(relacion.getUsuario().getId())
      ? relacion.getAmigo()
      : relacion.getUsuario();
  }

  private static int puntosDe(Usuario usuario) {
    if (usuario.getPerfil() == null || usuario.getPerfil().getPl() == null) {
      return 0;
    }
    return usuario.getPerfil().getPl();
  }
}
