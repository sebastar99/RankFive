package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioLoginImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Usuario consultarUsuario(String email, String password) {
    return repositorioUsuario.buscarUsuario(email, password);
  }

  @Override
  public void registrar(Usuario usuario) throws UsuarioExistente {
    Usuario usuarioConMismoEmail = repositorioUsuario.buscar(usuario.getEmail());
    Usuario usuarioConMismoNombre = repositorioUsuario.buscarPorNombreUsuario(
      usuario.getPerfil().getNombreUsuario()
    );
    if (usuarioConMismoEmail != null || usuarioConMismoNombre != null) {
      throw new UsuarioExistente();
    }
    if (usuario.getRol() == null || usuario.getRol().isBlank()) {
      usuario.setRol("JUGADOR");
    }
    if (usuario.getPerfil().getPl() == null) {
      usuario.getPerfil().setPl(1000);
    }
    repositorioUsuario.guardar(usuario);
  }
}
