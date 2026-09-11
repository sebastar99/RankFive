package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioPartido {
  Partido crearPartido(List<Usuario> equipoA, List<Usuario> equipoB);

  boolean registrarResultado(Long partidoId, int golesEquipoA, int golesEquipoB);

  List<Partido> listarPartidosDe(Usuario usuario);

  Usuario buscarPorEmail(String email);
}
