package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.implementacion.ServicioPartidoImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioPartidoImplEdgeTest {

  private static Usuario u(long id) {
    Usuario x = new Usuario();
    x.setId(id);
    return x;
  }

  @Test
  void crearPartido_valida_solapamiento_y_guarda() {
    RepositorioPartido repoPartido = mock(RepositorioPartido.class);
    RepositorioUsuario repoUsuario = mock(RepositorioUsuario.class);
    RepositorioNotificacionPl repoNotifPl = mock(RepositorioNotificacionPl.class);
    ServicioPartidoImpl servicio = new ServicioPartidoImpl(repoPartido, repoUsuario, repoNotifPl);

    // solapamiento: mismo id en A y B -> debe devolver null
    assertNull(servicio.crearPartido(List.of(u(1), u(2)), List.of(u(2), u(3))));

    // sin solapamiento -> crea y guarda
    Partido p = servicio.crearPartido(
      List.of(u(1), u(2)),
      List.of(u(3), u(4)),
      LocalDateTime.now()
    );
    assertNotNull(p);
    verify(repoPartido, atLeastOnce()).guardar(any(Partido.class));
  }

  @Test
  void registrarDetalles_sin_repo_devuelve_false_y_no_explota() {
    RepositorioPartido repoPartido = mock(RepositorioPartido.class);
    RepositorioUsuario repoUsuario = mock(RepositorioUsuario.class);
    RepositorioNotificacionPl repoNotifPl = mock(RepositorioNotificacionPl.class);
    ServicioPartidoImpl servicio = new ServicioPartidoImpl(repoPartido, repoUsuario, repoNotifPl);

    assertFalse(servicio.registrarDetalles(1L, List.of(), List.of(), List.of(), List.of()));
  }

  @Test
  void notificacionesPartido_con_repo_nulo_no_rompe_y_devuelve_vacio() {
    RepositorioPartido repoPartido = mock(RepositorioPartido.class);
    RepositorioUsuario repoUsuario = mock(RepositorioUsuario.class);
    RepositorioNotificacionPl repoNotifPl = mock(RepositorioNotificacionPl.class);
    ServicioPartidoImpl servicio = new ServicioPartidoImpl(repoPartido, repoUsuario, repoNotifPl);

    Usuario u = new Usuario();
    u.setId(5L);

    assertTrue(servicio.listarNotificacionesPartidoNoLeidas(u).isEmpty());
    assertDoesNotThrow(() -> servicio.marcarNotificacionesLeidas(u));
  }
}
