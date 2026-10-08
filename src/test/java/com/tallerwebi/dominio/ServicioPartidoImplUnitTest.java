package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.implementacion.ServicioPartidoImpl;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioPartidoImplUnitTest {

  private static Usuario u(long id, int pl) {
    Usuario x = new Usuario();
    x.setId(id);
    PerfilJugador pj = new PerfilJugador();
    pj.setPl(pl);
    x.setPerfil(pj);
    return x;
  }

  @Test
  void registrarResultado_aplica_delta_Elo_usando_constructor_reducido() {
    // repos mocks
    RepositorioPartido repoPartido = mock(RepositorioPartido.class);
    RepositorioUsuario repoUsuario = mock(RepositorioUsuario.class);
    RepositorioNotificacionPl repoNotifPl = mock(RepositorioNotificacionPl.class);

    // SUT con constructor reducido (crea SoportePartido real internamente)
    ServicioPartidoImpl servicio = new ServicioPartidoImpl(repoPartido, repoUsuario, repoNotifPl);

    // partido con equipos equilibrados (PL 1000), delta esperado: +16 / -16
    Usuario a1 = u(1L, 1000);
    Usuario b1 = u(2L, 1000);
    Set<Usuario> equipoA = new HashSet<>();
    equipoA.add(a1);
    Set<Usuario> equipoB = new HashSet<>();
    equipoB.add(b1);
    Partido partido = new Partido();
    partido.setId(10L);
    partido.setEquipoA(equipoA);
    partido.setEquipoB(equipoB);

    when(repoPartido.buscarPorId(10L)).thenReturn(partido);

    boolean ok = servicio.registrarResultado(10L, 1, 0);
    assertTrue(ok, "Debe registrar el resultado correctamente");

    // El PL de A sube y de B baja
    assertEquals(1016, a1.getPerfil().getPl());
    assertEquals(984, b1.getPerfil().getPl());

    // Se persisten modificaciones y notificaciones para ambos
    verify(repoUsuario, times(1)).modificar(a1);
    verify(repoUsuario, times(1)).modificar(b1);

    ArgumentCaptor<NotificacionPl> cap = ArgumentCaptor.forClass(NotificacionPl.class);
    verify(repoNotifPl, times(2)).guardar(cap.capture());
    // al menos una notificacion con delta positivo y otra negativo
    boolean hayPos = cap
      .getAllValues()
      .stream()
      .anyMatch(n -> n.getDelta() != null && n.getDelta() > 0);
    boolean hayNeg = cap
      .getAllValues()
      .stream()
      .anyMatch(n -> n.getDelta() != null && n.getDelta() < 0);
    assertTrue(hayPos && hayNeg, "Deben generarse notificaciones de delta positivo y negativo");

    // Se actualiza el partido
    verify(repoPartido, times(1)).actualizar(partido);
  }
}
