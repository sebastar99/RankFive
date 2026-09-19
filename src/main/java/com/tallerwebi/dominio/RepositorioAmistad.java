package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioAmistad {
  void guardar(Amistad amistad);

  void actualizar(Amistad amistad);

  Amistad buscarPorId(Long id);

  Amistad buscarRelacionEntre(Long unUsuarioId, Long otroUsuarioId);

  List<Usuario> listarAmigosDe(Long usuarioId);

  List<Amistad> listarSolicitudesPendientesPara(Long usuarioId);

  long contarSolicitudesPendientesPara(Long usuarioId);
}
