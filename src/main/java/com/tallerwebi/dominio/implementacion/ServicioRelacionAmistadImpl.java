package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.Amistad;
import com.tallerwebi.dominio.RepositorioAmistad;
import com.tallerwebi.dominio.ServicioRelacionAmistad;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioRelacionAmistad")
@Transactional
public class ServicioRelacionAmistadImpl implements ServicioRelacionAmistad {

  private final RepositorioAmistad repositorioAmistad;

  @Autowired
  public ServicioRelacionAmistadImpl(RepositorioAmistad repositorioAmistad) {
    this.repositorioAmistad = repositorioAmistad;
  }

  @Override
  public void agregarAmigo(Usuario usuarioActual, Usuario usuarioAmigo) {
    if (usuarioActual == null || usuarioAmigo == null) {
      return;
    }
    if (usuarioActual.getId() != null && usuarioAmigo.getId() != null) {
      if (usuarioActual.getId().equals(usuarioAmigo.getId())) {
        return; // no agregarse a sí mismo
      }
      if (repositorioAmistad.existeRelacion(usuarioActual.getId(), usuarioAmigo.getId())) {
        return; // ya existe
      }
    }
    Amistad amistad = new Amistad();
    amistad.setUsuario(usuarioActual);
    amistad.setAmigo(usuarioAmigo);
    repositorioAmistad.guardar(amistad);
  }

  @Override
  public List<Usuario> listarAmigosDe(Usuario usuarioActual) {
    if (usuarioActual == null || usuarioActual.getId() == null) {
      return java.util.Collections.emptyList();
    }
    return repositorioAmistad.listarAmigosDe(usuarioActual.getId());
  }
}
