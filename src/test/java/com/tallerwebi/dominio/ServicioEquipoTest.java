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

import com.tallerwebi.dominio.enums.ResultadoTransferencia;
import com.tallerwebi.dominio.implementacion.ServicioEquipoImpl;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioEquipoTest {

  private RepositorioEquipo repositorioEquipo;
  private RepositorioInvitacionEquipo repositorioInvitacion;
  private ServicioRelacionAmistad servicioAmistad;
  private ServicioEquipoImpl servicio;
  private Equipo equipoDestino;
  private Equipo equipoOrigen;

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
    when(servicioAmistad.listarAmigosDe(owner))
      .thenReturn(
        List.of(
          amigo1,
          amigo2,
          usuario(9L, "user09", 2000),
          usuario(10L, "user10", 2100),
          usuario(11L, "user11", 2200)
        )
      );
    equipoDestino = equipoPendienteCon(owner);
    equipoDestino.setEstado(EstadoEquipo.ACTIVO);

    equipoOrigen = new Equipo();
    equipoOrigen.setId(200L);
    equipoOrigen.setNombre("Rival");
    equipoOrigen.setFormato(5);
    equipoOrigen.setOwner(amigo2);
    equipoOrigen.agregarJugador(amigo1);
    equipoOrigen.agregarJugador(amigo2);

    when(repositorioEquipo.buscarPorId(100L)).thenReturn(equipoDestino);
    when(repositorioEquipo.buscarPorId(200L)).thenReturn(equipoOrigen);
  }

  @Test
  public void crearEquipoGuardaEquipoPendienteConOwnerEInvitaAmigos() {
    ResultadoCreacionEquipo resultado = servicio.crearEquipo(
      owner,
      "Los Pibes",
      5,
      "/escudos/x.png",
      List.of("user07", "user08", "user09", "user10")
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
    verify(repositorioInvitacion, times(4)).guardar(invCaptor.capture());
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
      List.of("user07", "user08", "user09", "desconocido")
    );

    assertThat(resultado, equalTo(ResultadoCreacionEquipo.JUGADOR_NO_AMIGO));
    verify(repositorioEquipo, never()).guardar(any());
  }

  @Test
  public void permiteMasInvitadosQueElFormato() {
    ResultadoCreacionEquipo resultado = servicio.crearEquipo(
      owner,
      "Los Pibes",
      5,
      null,
      List.of("user07", "user08", "user09", "user10", "user11")
    );

    assertThat(resultado, equalTo(ResultadoCreacionEquipo.CREADO));
    verify(repositorioInvitacion, times(5)).guardar(any());
  }

  @Test
  public void exigeElMinimoDeJugadoresParaCadaFormato() {
    for (int formato : List.of(5, 7, 8, 9, 11)) {
      List<String> nombres = nombresDeAmigos(formato);

      ResultadoCreacionEquipo insuficiente = servicio.crearEquipo(
        owner,
        "Equipo",
        formato,
        null,
        nombres.subList(0, formato - 2)
      );
      assertThat(insuficiente, equalTo(ResultadoCreacionEquipo.JUGADORES_INSUFICIENTES));
      verify(repositorioEquipo, never()).guardar(any());
    }
  }

  @Test
  public void permiteElMinimoExactoParaCadaFormato() {
    for (int formato : List.of(5, 7, 8, 9, 11)) {
      List<String> nombres = nombresDeAmigos(formato);
      assertThat(
        servicio.crearEquipo(owner, "Equipo", formato, null, nombres.subList(0, formato - 1)),
        equalTo(ResultadoCreacionEquipo.CREADO)
      );
    }
    verify(repositorioEquipo, times(5)).guardar(any());
  }

  @Test
  public void nombresRepetidosYElOwnerNoCuentanParaElMinimo() {
    ResultadoCreacionEquipo resultado = servicio.crearEquipo(
      owner,
      "Equipo",
      5,
      null,
      List.of("user07", "user07", "user08", "user02")
    );

    assertThat(resultado, equalTo(ResultadoCreacionEquipo.JUGADORES_INSUFICIENTES));
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

  @Test
  public void ofrecerTransferenciaCreaInvitacionPendienteConOrigenYDestino() {
    ResultadoTransferencia r = servicio.ofrecerTransferencia(owner, 100L, 200L, 7L);

    assertThat(r, equalTo(ResultadoTransferencia.OFERTA_ENVIADA));
    ArgumentCaptor<InvitacionEquipo> cap = ArgumentCaptor.forClass(InvitacionEquipo.class);
    verify(repositorioInvitacion).guardar(cap.capture());
    assertThat(cap.getValue().getEquipo(), equalTo(equipoDestino));
    assertThat(cap.getValue().getEquipoOrigen(), equalTo(equipoOrigen));
    assertThat(cap.getValue().getUsuario(), equalTo(amigo1));
    assertThat(cap.getValue().estaPendiente(), is(true));
  }

  @Test
  public void soloElCapitanDelEquipoDestinoPuedeOfrecer() {
    ResultadoTransferencia r = servicio.ofrecerTransferencia(amigo1, 100L, 200L, 8L);
    assertThat(r, equalTo(ResultadoTransferencia.NO_SOS_CAPITAN));
    verify(repositorioInvitacion, never()).guardar(any());
  }

  @Test
  public void noOfreceJugadorQueNoJuegaEnElEquipoOrigen() {
    assertThat(
      servicio.ofrecerTransferencia(owner, 100L, 200L, 999L),
      equalTo(ResultadoTransferencia.JUGADOR_NO_PERTENECE)
    );
    verify(repositorioInvitacion, never()).guardar(any());
  }

  @Test
  public void noOfreceJugadorQueYaEsIntegranteDelDestino() {
    equipoDestino.agregarJugador(amigo1);

    assertThat(
      servicio.ofrecerTransferencia(owner, 100L, 200L, 7L),
      equalTo(ResultadoTransferencia.YA_ES_INTEGRANTE)
    );
  }

  @Test
  public void noDuplicaOfertasPendientesAlMismoJugador() {
    InvitacionEquipo pendiente = invitacion(
      50L,
      equipoDestino,
      amigo1,
      EstadoInvitacionEquipo.PENDIENTE
    );
    when(repositorioInvitacion.listarDeEquipo(100L)).thenReturn(List.of(pendiente));

    assertThat(
      servicio.ofrecerTransferencia(owner, 100L, 200L, 7L),
      equalTo(ResultadoTransferencia.OFERTA_YA_PENDIENTE)
    );
  }

  @Test
  public void alAceptarLaOfertaElJugadorSaleDelOrigenYEntraAlDestino() {
    completarPlantel(equipoOrigen, 6);
    InvitacionEquipo oferta = invitacion(
      60L,
      equipoDestino,
      amigo1,
      EstadoInvitacionEquipo.PENDIENTE
    );
    oferta.setEquipoOrigen(equipoOrigen);
    when(repositorioInvitacion.buscarPorId(60L)).thenReturn(oferta);
    when(repositorioInvitacion.listarDeEquipo(100L)).thenReturn(List.of(oferta));
    assertThat(servicio.aceptarInvitacion(60L, amigo1), is(true));

    assertThat(equipoDestino.getJugadores().contains(amigo1), is(true));
    assertThat(equipoOrigen.getJugadores().contains(amigo1), is(false));
    verify(repositorioEquipo).actualizar(equipoOrigen);
  }

  @Test
  public void alRechazarLaOfertaElJugadorNoCambiaDeEquipo() {
    InvitacionEquipo oferta = invitacion(
      60L,
      equipoDestino,
      amigo1,
      EstadoInvitacionEquipo.PENDIENTE
    );
    oferta.setEquipoOrigen(equipoOrigen);
    when(repositorioInvitacion.buscarPorId(60L)).thenReturn(oferta);
    when(repositorioInvitacion.listarDeEquipo(100L)).thenReturn(List.of(oferta));
    assertThat(servicio.rechazarInvitacion(60L, amigo1), is(true));

    assertThat(equipoDestino.getJugadores().contains(amigo1), is(false));
    assertThat(equipoOrigen.getJugadores().contains(amigo1), is(true));
    verify(repositorioEquipo, never()).actualizar(equipoOrigen);
  }

  @Test
  public void elJugadorIntentaEnviarDosVecesLaMismaOferta() {
    InvitacionEquipo pendiente = invitacion(
      60L,
      equipoDestino,
      amigo1,
      EstadoInvitacionEquipo.PENDIENTE
    );
    pendiente.setEquipoOrigen(equipoOrigen);
    when(repositorioInvitacion.buscarPorId(60L)).thenReturn(pendiente);
    when(repositorioInvitacion.listarDeEquipo(100L)).thenReturn(List.of(pendiente));
    ResultadoTransferencia resultado = servicio.ofrecerTransferencia(
      owner,
      equipoDestino.getId(),
      equipoOrigen.getId(),
      amigo1.getId()
    );
    assertThat(resultado, is(ResultadoTransferencia.OFERTA_YA_PENDIENTE));
    verify(repositorioInvitacion, never()).guardar(any());
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

  @Test
  public void noAceptaPasesQueDejanAlOrigenPorDebajoDelMinimoEnNingunFormato() {
    for (int formato : List.of(5, 7, 8, 9, 11)) {
      Equipo origen = equipoPendienteCon(amigo2);
      origen.setId(200L);
      origen.setFormato(formato);
      origen.agregarJugador(amigo1);
      completarPlantel(origen, formato);
      InvitacionEquipo oferta = invitacion(
        (long) formato,
        equipoDestino,
        amigo1,
        EstadoInvitacionEquipo.PENDIENTE
      );
      oferta.setEquipoOrigen(origen);
      when(repositorioInvitacion.buscarPorId(oferta.getId())).thenReturn(oferta);

      assertThat(servicio.aceptarInvitacion(oferta.getId(), amigo1), is(false));
      assertThat(oferta.getEstado(), equalTo(EstadoInvitacionEquipo.PENDIENTE));
      assertThat(origen.getJugadores(), hasSize(formato));
      assertThat(equipoDestino.getJugadores().contains(amigo1), is(false));
    }
    verify(repositorioInvitacion, never()).actualizar(any());
    verify(repositorioEquipo, never()).actualizar(any());
  }

  @Test
  public void permitePasesCuandoQuedaElMinimoExactoParaTodosLosFormatos() {
    for (int formato : List.of(5, 7, 8, 9, 11)) {
      Equipo origen = equipoPendienteCon(amigo2);
      origen.setId(200L);
      Equipo destino = equipoPendienteCon(owner);
      origen.setFormato(formato);
      origen.agregarJugador(amigo1);
      completarPlantel(origen, formato + 1);
      InvitacionEquipo oferta = invitacion(
        (long) formato,
        destino,
        amigo1,
        EstadoInvitacionEquipo.PENDIENTE
      );
      oferta.setEquipoOrigen(origen);
      when(repositorioInvitacion.buscarPorId(oferta.getId())).thenReturn(oferta);

      assertThat(servicio.aceptarInvitacion(oferta.getId(), amigo1), is(true));
      assertThat(oferta.getEstado(), equalTo(EstadoInvitacionEquipo.ACEPTADA));
      assertThat(origen.getJugadores(), hasSize(formato));
      assertThat(origen.getJugadores().contains(amigo1), is(false));
      assertThat(destino.getJugadores().contains(amigo1), is(true));
    }
  }

  @Test
  public void tampocoAceptaSiElOrigenYaEstabaPorDebajoDelMinimo() {
    completarPlantel(equipoOrigen, 4);
    InvitacionEquipo oferta = invitacion(
      60L,
      equipoDestino,
      amigo1,
      EstadoInvitacionEquipo.PENDIENTE
    );
    oferta.setEquipoOrigen(equipoOrigen);
    when(repositorioInvitacion.buscarPorId(60L)).thenReturn(oferta);

    assertThat(servicio.aceptarInvitacion(60L, amigo1), is(false));
    verify(repositorioEquipo, never()).actualizar(any());
    verify(repositorioInvitacion, never()).actualizar(any());
  }

  private static void completarPlantel(Equipo equipo, int cantidad) {
    while (equipo.getJugadores().size() < cantidad) {
      int numero = equipo.getJugadores().size();
      equipo.agregarJugador(usuario(1000L + numero, "suplente" + numero, 1000));
    }
  }

  private List<String> nombresDeAmigos(int cantidad) {
    List<Usuario> amigos = IntStream
      .range(0, cantidad)
      .mapToObj(i -> usuario(100L + i, "amigo" + i, 1000))
      .toList();
    when(servicioAmistad.listarAmigosDe(owner)).thenReturn(amigos);
    return amigos.stream().map(a -> a.getPerfil().getNombreUsuario()).toList();
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
