package com.tallerwebi.dominio.implementacion;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioAmigos;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioAmigos")
@Transactional
public class ServicioAmigosImpl implements ServicioAmigos {

  private RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioAmigosImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Usuario buscarAmigoPorNombreDeUsuario(String nombreUsuario) {
    return repositorioUsuario.buscarPorNombreUsuario(nombreUsuario);
  }
}
