package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.implementacion.ServicioEquipoImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioEquipoTest {

  private RepositorioEquipo repositorioEquipo;
  private RepositorioInvitacionEquipo repositorioInvitacion;
  private ServicioRelacionAmistad servicioAmistad;
  private ServicioEquipoImpl servicio;

  private Usuario owner;
  private Usuario amigo1;
  private Usuario amigo2;

  @BeforeEach
  public void init() {
    repositorioEquipo = mock(RepositorioEquipo.class);
    repositorioInvitacion = mock(RepositorioInvitacionEquipo.class);
    servicioAmistad = mock(ServicioRelacionAmistad.class);
    servicio = new ServicioEquipoImpl(repositorioEquipo, repositorioInvitacion, servicioAmistad);

    owner = usuario(1L, "user02", 1450);
    amigo1 = usuario(7L, "user07", 2600);
    amigo2 = usuario(8L, "user08", 2750);
    when(servicioAmistad.listarAmigosDe(owner)).thenReturn(List.of(amigo1, amigo2));
  }

  @Test
  public void crearEquipoGuardaEquipoPendienteConOwnerEInvitaAmigos() {
    ResultadoCreacionEquipo resultado = servicio.crearEquipo(
      owner,
      "Los Pibes",
      5,
      "/escudos/x.png",
      List.of("user07", "user08")
    );

    assertThat(resultado, equalTo(ResultadoCreacionEquipo.CREADO));

    ArgumentCaptor<Equipo> equipoCaptor = ArgumentCaptor.forClass(Equipo.class);
    verify(repositorioEquipo).guardar(equipoCaptor.capture());
    Equipo guardado = equipoCaptor.getValue();
    assertThat(guardado.getEstado(), equalTo(EstadoEquipo.PENDIENTE));
    assertThat(guardado.getOwner(), equalTo(owner));
    assertThat(guardado.getJugadores(), hasSize(1));
    assertThat(guardado.getFotoUrl(), equalTo("/escudos/x.png"));

    ArgumentCaptor<InvitacionEquipo> invCaptor = ArgumentCaptor.forClass(InvitacionEquipo.class);
    verify(repositorioInvitacion, times(2)).guardar(invCaptor.capture());
    assertThat(invCaptor.getAllValues().get(0).getUsuario(), equalTo(amigo1));
    assertThat(invCaptor.getAllValues().get(1).getUsuario(), equalTo(amigo2));
    assertThat(invCaptor.getAllValues().get(0).estaPendiente(), is(true));
  }

  @Test
  public void noPermiteInvitarAQuienNoEsAmigo() {
    ResultadoCreacionEquipo resultado = servicio.crearEquipo(
      owner,
      "Los Pibes",
      5,
      null,
      List.of("desconocido")
    );

    assertThat(resultado, equalTo(ResultadoCreacionEquipo.JUGADOR_NO_AMIGO));
    verify(repositorioEquipo, never()).guardar(any());
  }

  @Test
  public void noPermiteMasInvitadosQueElFormato() {
    ResultadoCreacionEquipo resultado = servicio.crearEquipo(
      owner,
      "Los Pibes",
      5,
      null,
      List.of("a", "b", "c", "d", "e")
    );

    assertThat(resultado, equalTo(ResultadoCreacionEquipo.DEMASIADOS_JUGADORES));
    verify(repositorioEquipo, never()).guardar(any());
  }

  @Test
  public void rechazaNombreVacioYFormatoInvalido() {
    assertThat(
      servicio.crearEquipo(owner, "  ", 5, null, List.of("user07")),
      equalTo(ResultadoCreacionEquipo.NOMBRE_INVALIDO)
    );
    assertThat(
      servicio.crearEquipo(owner, "Equipo", 6, null, List.of("user07")),
      equalTo(ResultadoCreacionEquipo.FORMATO_INVALIDO)
    );
    assertThat(
      servicio.crearEquipo(owner, "Equipo", 5, null, List.of()),
      equalTo(ResultadoCreacionEquipo.SIN_JUGADORES)
    );
  }

  @Test
  public void alAceptarLaUltimaInvitacionElEquipoQuedaActivo() {
    Equipo equipo = equipoPendienteCon(owner);
    InvitacionEquipo inv1 = invitacion(10L, equipo, amigo1, EstadoInvitacionEquipo.ACEPTADA);
    InvitacionEquipo inv2 = invitacion(11L, equipo, amigo2, EstadoInvitacionEquipo.PENDIENTE);
    when(repositorioInvitacion.buscarPorId(11L)).thenReturn(inv2);
    when(repositorioInvitacion.listarDeEquipo(equipo.getId())).thenReturn(List.of(inv1, inv2));

    boolean ok = servicio.aceptarInvitacion(11L, amigo2);

    assertThat(ok, is(true));
    assertThat(inv2.getEstado(), equalTo(EstadoInvitacionEquipo.ACEPTADA));
    assertThat(equipo.getEstado(), equalTo(EstadoEquipo.ACTIVO));
    assertThat(equipo.getJugadores(), hasSize(2));
    verify(repositorioEquipo).actualizar(equipo);
  }

  @Test
  public void siQuedanInvitacionesPendientesElEquipoSigueSinActivarse() {
    Equipo equipo = equipoPendienteCon(owner);
    InvitacionEquipo inv1 = invitacion(10L, equipo, amigo1, EstadoInvitacionEquipo.PENDIENTE);
    InvitacionEquipo inv2 = invitacion(11L, equipo, amigo2, EstadoInvitacionEquipo.PENDIENTE);
    when(repositorioInvitacion.buscarPorId(10L)).thenReturn(inv1);
    when(repositorioInvitacion.listarDeEquipo(equipo.getId())).thenReturn(List.of(inv1, inv2));

    servicio.aceptarInvitacion(10L, amigo1);

    assertThat(equipo.getEstado(), equalTo(EstadoEquipo.PENDIENTE));
    assertThat(equipo.getJugadores(), hasSize(2));
  }

  @Test
  public void soloElDestinatarioPuedeResponderLaInvitacion() {
    Equipo equipo = equipoPendienteCon(owner);
    InvitacionEquipo inv = invitacion(10L, equipo, amigo1, EstadoInvitacionEquipo.PENDIENTE);
    when(repositorioInvitacion.buscarPorId(10L)).thenReturn(inv);

    assertThat(servicio.aceptarInvitacion(10L, amigo2), is(false));
    assertThat(servicio.rechazarInvitacion(10L, owner), is(false));
    assertThat(inv.getEstado(), equalTo(EstadoInvitacionEquipo.PENDIENTE));
  }

  @Test
  public void rechazarMarcaLaInvitacionYNoTocaElEquipo() {
    Equipo equipo = equipoPendienteCon(owner);
    InvitacionEquipo inv = invitacion(10L, equipo, amigo1, EstadoInvitacionEquipo.PENDIENTE);
    when(repositorioInvitacion.buscarPorId(10L)).thenReturn(inv);

    assertThat(servicio.rechazarInvitacion(10L, amigo1), is(true));
    assertThat(inv.getEstado(), equalTo(EstadoInvitacionEquipo.RECHAZADA));
    verify(repositorioEquipo, never()).actualizar(any());
  }

  @Test
  public void elPromedioYRangoDelEquipoSeCalculanConSusJugadores() {
    Equipo equipo = equipoPendienteCon(owner);
    equipo.agregarJugador(amigo1);
    equipo.agregarJugador(amigo2);

    // (1450 + 2600 + 2750) / 3 = 2267 -> Semiprofesional (1998-2496)
    assertThat(equipo.getPromedioPl(), equalTo(2267));
    assertThat(equipo.getRango(), equalTo(RangoEquipo.SEMIPROFESIONAL));
  }

  private static Usuario usuario(Long id, String nombreUsuario, int pl) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setEmail(nombreUsuario + "@rankfive.com");
    PerfilJugador perfil = new PerfilJugador();
    perfil.setNombreUsuario(nombreUsuario);
    perfil.setPl(pl);
    usuario.setPerfil(perfil);
    return usuario;
  }

  private static Equipo equipoPendienteCon(Usuario owner) {
    Equipo equipo = new Equipo();
    equipo.setId(100L);
    equipo.setNombre("Equipo");
    equipo.setFormato(5);
    equipo.setOwner(owner);
    equipo.agregarJugador(owner);
    return equipo;
  }

  private static InvitacionEquipo invitacion(
    Long id,
    Equipo equipo,
    Usuario usuario,
    EstadoInvitacionEquipo estado
  ) {
    InvitacionEquipo inv = new InvitacionEquipo();
    inv.setId(id);
    inv.setEquipo(equipo);
    inv.setUsuario(usuario);
    inv.setEstado(estado);
    return inv;
  }
}
