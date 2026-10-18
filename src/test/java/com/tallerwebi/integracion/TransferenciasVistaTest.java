package com.tallerwebi.integracion;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Equipo;
import com.tallerwebi.dominio.EstadoEquipo;
import com.tallerwebi.dominio.EstadoInvitacionEquipo;
import com.tallerwebi.dominio.InscripcionLiga;
import com.tallerwebi.dominio.InscripcionTorneo;
import com.tallerwebi.dominio.InvitacionEquipo;
import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.ServicioEquipo;
import com.tallerwebi.dominio.Torneo;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.ResultadoTransferencia;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
@Transactional
public class TransferenciasVistaTest {

  private static final String AVISO_MINIMO =
    "No se puede realizar la transferencia porque el equipo no cuenta con la suficiente cantidad de integrantes";

  @Autowired
  private WebApplicationContext contexto;

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  private ServicioEquipo servicioEquipo;

  private MockMvc mockMvc;
  private Usuario capitan;
  private Usuario jugador;
  private Equipo destino;
  private Equipo origen;
  private Torneo torneo;

  @BeforeEach
  public void preparar() {
    mockMvc = MockMvcBuilders.webAppContextSetup(requireNonNull(contexto)).build();
    capitan = usuario("capitanDestino");
    jugador = usuario("jugadorOfertado");
    destino = equipo("SacaPuntos FC", capitan);
    origen = equipo("Equipo origen", usuario("capitanOrigen"));
    origen.agregarJugador(jugador);
    torneo = new Torneo();
    torneo.setNombre("Copa de pases");
    torneo.setFormato(5);
    sessionFactory.getCurrentSession().persist(torneo);
    inscribir(destino);
    inscribir(origen);
    recargar();
  }

  @Test
  public void muestraFormularioDeTransferenciaYExplicaQueRequiereAceptacion() throws Exception {
    String html = pantallaMercado(capitan, origen, jugador);

    assertThat(html, containsString("Iniciar transferencia"));
    assertThat(html, containsString("action=\"/transferencias/ofertar\""));
    assertThat(html, containsString("name=\"jugador\" value=\"" + jugador.getId() + "\""));
    assertThat(html, containsString("El pase solo se completará cuando el jugador acepte"));
    assertThat(html, containsString("SacaPuntos FC"));
    assertThat(html, not(containsString("Iniciar conversación")));
  }

  @Test
  public void ocultaElFormularioParaJugadoresDelPropioEquipoYRechazaElPost() throws Exception {
    String html = pantallaMercado(capitan, destino, capitan);
    assertThat(html, not(containsString("action=\"/transferencias/ofertar\"")));
    assertThat(html, containsString("Este jugador ya forma parte de tu equipo"));

    enviarOferta(capitan, destino, capitan);
    assertEquals(0L, cantidadOfertas());
  }

  @Test
  public void tampocoOfreceAUnIntegrantePropioAunqueFigureEnOtroEquipo() throws Exception {
    sessionFactory
      .getCurrentSession()
      .get(Equipo.class, destino.getId())
      .agregarJugador(sessionFactory.getCurrentSession().get(Usuario.class, jugador.getId()));
    recargar();

    assertThat(
      pantallaMercado(capitan, origen, jugador),
      not(containsString("action=\"/transferencias/ofertar\""))
    );
    enviarOferta(capitan, origen, jugador);
    assertEquals(0L, cantidadOfertas());
  }

  @Test
  public void soloElCapitanPuedeEnviarInclusoSinUsarElFormulario() throws Exception {
    assertThat(
      pantallaMercado(jugador, destino, capitan),
      not(containsString("action=\"/transferencias/ofertar\""))
    );
    enviarOferta(jugador, origen, jugador);
    assertEquals(0L, cantidadOfertas());
    mockMvc
      .perform(
        post("/transferencias/ofertar")
          .param("id", torneo.getId().toString())
          .param("equipo", origen.getId().toString())
          .param("jugador", jugador.getId().toString())
      )
      .andExpect(redirectedUrl("/login"));
  }

  @Test
  public void enviarNotificaSoloAlDestinatarioSinMoverloNiDuplicarOfertas() throws Exception {
    String redireccion = enviarOferta(capitan, origen, jugador);
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();

    assertEquals(1L, cantidadOfertas());
    assertTrue(pertenece(origen, jugador));
    assertFalse(pertenece(destino, jugador));
    assertEquals(EstadoInvitacionEquipo.PENDIENTE, oferta().getEstado());
    String aviso = requireNonNull(redireccion).substring(redireccion.indexOf("&aviso=") + 7);
    String confirmacion = html(
      get("/transferencias")
        .param("id", torneo.getId().toString())
        .param("aviso", URLDecoder.decode(aviso, StandardCharsets.UTF_8)),
      capitan
    );
    assertThat(confirmacion, containsString("Oferta enviada"));
    assertThat(confirmacion, containsString("El jugador puede aceptarla o rechazarla"));

    String notificacion = pantallaMercado(jugador, origen, jugador);
    assertThat(notificacion, containsString("quiere que te sumes a"));
    assertThat(notificacion, containsString("SacaPuntos FC"));
    assertThat(notificacion, containsString("Ver equipo y responder"));
    assertThat(notificacion, containsString("/transferencias/ofertas/" + ofertaId));
    assertThat(
      pantallaMercado(capitan, origen, jugador),
      not(containsString("/transferencias/ofertas/" + ofertaId))
    );
  }

  @Test
  public void destinatarioVeEquipoCapitanPlantelYAccionesAntesDeDecidir() throws Exception {
    enviarOferta(capitan, origen, jugador);
    String html = html(get("/transferencias/ofertas/" + oferta().getId()), jugador);

    assertThat(html, containsString("SacaPuntos FC"));
    assertThat(html, containsString("capitanDestino"));
    assertThat(html, containsString("Plantel"));
    assertThat(html, containsString("F5"));
    assertThat(html, containsString("PL promedio"));
    assertThat(html, containsString("Equipo origen"));
    assertThat(html, containsString("Si aceptás, dejarás"));
    assertThat(html, containsString("Aceptar pase"));
    assertThat(html, containsString("Rechazar pase"));
    assertThat(html, containsString("action=\"/equipos/invitaciones/aceptar\""));
    assertThat(html, containsString("action=\"/equipos/invitaciones/rechazar\""));
    assertTrue(pertenece(origen, jugador));
    assertFalse(pertenece(destino, jugador));
  }

  @Test
  public void aceptarPersisteElPaseYLoMuestraEnElNuevoEquipo() throws Exception {
    completarPlantel(sessionFactory.getCurrentSession().get(Equipo.class, origen.getId()), 6);
    recargar();
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    responder("aceptar", ofertaId, jugador);

    assertEquals(EstadoInvitacionEquipo.ACEPTADA, oferta().getEstado());
    assertFalse(pertenece(origen, jugador));
    assertTrue(pertenece(destino, jugador));
    assertThat(html(get("/dashboard"), jugador), containsString("SacaPuntos FC"));
    assertThat(
      pantallaMercado(jugador, destino, jugador),
      not(containsString("/transferencias/ofertas/" + ofertaId))
    );
    responder("aceptar", ofertaId, jugador);
    assertEquals(
      2,
      sessionFactory.getCurrentSession().get(Equipo.class, destino.getId()).getJugadores().size()
    );
    mockMvc
      .perform(
        get("/transferencias/ofertas/" + ofertaId).sessionAttr("usuario", requireNonNull(jugador))
      )
      .andExpect(status().isNotFound());
  }

  @Test
  public void rechazarConservaPlantelesYEliminaLaNotificacionPendiente() throws Exception {
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    responder("rechazar", ofertaId, jugador);
    responder("aceptar", ofertaId, jugador);

    assertEquals(EstadoInvitacionEquipo.RECHAZADA, oferta().getEstado());
    assertTrue(pertenece(origen, jugador));
    assertFalse(pertenece(destino, jugador));
    assertThat(
      pantallaMercado(jugador, origen, jugador),
      not(containsString("/transferencias/ofertas/" + ofertaId))
    );
  }

  @Test
  public void nadieMasPuedeConsultarNiResponderLaOferta() throws Exception {
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    mockMvc.perform(get("/transferencias/ofertas/" + ofertaId)).andExpect(redirectedUrl("/login"));
    mockMvc
      .perform(
        get("/transferencias/ofertas/" + ofertaId).sessionAttr("usuario", requireNonNull(capitan))
      )
      .andExpect(status().isNotFound());
    mockMvc
      .perform(
        get("/transferencias/ofertas/999999").sessionAttr("usuario", requireNonNull(jugador))
      )
      .andExpect(status().isNotFound());
    responder("aceptar", ofertaId, capitan);
    responder("rechazar", ofertaId, capitan);

    assertEquals(EstadoInvitacionEquipo.PENDIENTE, oferta().getEstado());
    assertTrue(pertenece(origen, jugador));
    assertFalse(pertenece(destino, jugador));
  }

  @Test
  public void noAceptaOfertasViejasSiElJugadorYaNoEstaEnElOrigen() throws Exception {
    enviarOferta(capitan, origen, jugador);
    sessionFactory.getCurrentSession().get(Equipo.class, origen.getId()).quitarJugador(jugador);
    recargar();
    responder("aceptar", oferta().getId(), jugador);

    assertFalse(pertenece(destino, jugador));
    assertEquals(EstadoInvitacionEquipo.PENDIENTE, oferta().getEstado());
  }

  @Test
  public void lasInvitacionesDeCreacionConservanSusBotonesYNoSonPases() throws Exception {
    InvitacionEquipo invitacion = new InvitacionEquipo();
    invitacion.setEquipo(sessionFactory.getCurrentSession().get(Equipo.class, destino.getId()));
    invitacion.setUsuario(sessionFactory.getCurrentSession().get(Usuario.class, jugador.getId()));
    sessionFactory.getCurrentSession().persist(invitacion);
    recargar();

    String html = pantallaMercado(jugador, origen, jugador);
    assertThat(html, containsString("action=\"/equipos/invitaciones/aceptar\""));
    assertThat(html, containsString("action=\"/equipos/invitaciones/rechazar\""));
    assertThat(html, not(containsString("Ver equipo y responder")));
    mockMvc
      .perform(
        get("/transferencias/ofertas/" + invitacion.getId())
          .sessionAttr("usuario", requireNonNull(jugador))
      )
      .andExpect(status().isNotFound());
  }

  @Test
  public void losAvisosSeMuestranComoTextoSinEjecutarHtml() throws Exception {
    String html = html(
      get("/transferencias")
        .param("id", torneo.getId().toString())
        .param("aviso", "<script>alert(1)</script>"),
      capitan
    );
    assertThat(html, containsString("&lt;script&gt;"));
    assertThat(html, not(containsString("<script>alert(1)</script>")));
  }

  @Test
  public void elDetalleCargaLosDatosNecesariosAntesDeCerrarLaSesionHibernate() throws Exception {
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    recargar();
    InvitacionEquipo detalle = servicioEquipo.buscarOfertaPendiente(ofertaId, jugador);
    sessionFactory.getCurrentSession().clear();

    assertEquals("SacaPuntos FC", detalle.getEquipo().getNombre());
    assertEquals("capitanDestino", detalle.getEquipo().getOwner().getPerfil().getNombreUsuario());
    assertEquals(1, detalle.getEquipo().getJugadores().size());
    assertEquals(1000, detalle.getEquipo().getPromedioPl());
    assertEquals("Equipo origen", detalle.getEquipoOrigen().getNombre());
  }

  @Test
  public void elPostBloqueaElPaseYExplicaElMinimoDeCadaLigaYTorneo() throws Exception {
    for (String tipo : List.of("liga", "torneo")) {
      for (int formato : List.of(5, 7, 8, 9, 11)) {
        comprobarMinimoCompetencia(tipo, formato, false);
      }
    }
  }

  @Test
  public void elPostPermiteElPaseSiQuedaElMinimoExactoDeCadaLigaYTorneo() throws Exception {
    for (String tipo : List.of("liga", "torneo")) {
      for (int formato : List.of(5, 7, 8, 9, 11)) {
        comprobarMinimoCompetencia(tipo, formato, true);
      }
    }
  }

  @Test
  public void vuelveAContarElPlantelAlAceptarAunqueLaOfertaSeEnvioConSuplentes() throws Exception {
    Equipo actual = sessionFactory.getCurrentSession().get(Equipo.class, origen.getId());
    completarPlantel(actual, 6);
    Usuario suplente = actual
      .getJugadores()
      .stream()
      .filter(j -> j.getPerfil().getNombreUsuario().startsWith("suplente"))
      .findFirst()
      .orElseThrow();
    enviarOferta(capitan, origen, jugador);
    sessionFactory.getCurrentSession().get(Equipo.class, origen.getId()).quitarJugador(suplente);
    recargar();

    String redireccion = responder("aceptar", oferta().getId(), jugador);

    assertThat(
      URLDecoder.decode(requireNonNull(redireccion), StandardCharsets.UTF_8),
      containsString(AVISO_MINIMO)
    );
    assertTrue(pertenece(origen, jugador));
    assertFalse(pertenece(destino, jugador));
    assertEquals(EstadoInvitacionEquipo.PENDIENTE, oferta().getEstado());
  }

  @Test
  public void respetaLaCompetenciaMasExigenteDelOrigen() throws Exception {
    Equipo actual = sessionFactory.getCurrentSession().get(Equipo.class, origen.getId());
    completarPlantel(actual, 8);
    inscribirEnCompetencia("liga", 9, actual);
    enviarOferta(capitan, origen, jugador);

    String redireccion = responder("aceptar", oferta().getId(), jugador);

    assertThat(
      URLDecoder.decode(requireNonNull(redireccion), StandardCharsets.UTF_8),
      containsString(AVISO_MINIMO)
    );
    assertTrue(pertenece(origen, jugador));
    assertFalse(pertenece(destino, jugador));
  }

  private void comprobarMinimoCompetencia(String tipo, int formato, boolean permitido)
    throws Exception {
    Equipo desde = equipo("Origen " + tipo + formato, usuario("owner" + tipo + formato));
    desde.agregarJugador(sessionFactory.getCurrentSession().get(Usuario.class, jugador.getId()));
    completarPlantel(desde, formato + (permitido ? 1 : 0));
    Equipo hacia = equipo(
      "Destino " + tipo + formato,
      sessionFactory.getCurrentSession().get(Usuario.class, capitan.getId())
    );
    inscribirEnCompetencia(tipo, formato, desde);
    recargar();
    assertEquals(
      ResultadoTransferencia.OFERTA_ENVIADA,
      servicioEquipo.ofrecerTransferencia(capitan, hacia.getId(), desde.getId(), jugador.getId())
    );
    recargar();
    Long ofertaId = sessionFactory
      .getCurrentSession()
      .createQuery("select i.id from InvitacionEquipo i where i.equipo.id = :destino", Long.class)
      .setParameter("destino", hacia.getId())
      .getSingleResult();

    String redireccion = responder("aceptar", ofertaId, jugador);

    assertEquals(!permitido, pertenece(desde, jugador));
    assertEquals(permitido, pertenece(hacia, jugador));
    assertEquals(
      formato,
      sessionFactory.getCurrentSession().get(Equipo.class, desde.getId()).getJugadores().size()
    );
    assertEquals(
      permitido ? EstadoInvitacionEquipo.ACEPTADA : EstadoInvitacionEquipo.PENDIENTE,
      sessionFactory.getCurrentSession().get(InvitacionEquipo.class, ofertaId).getEstado()
    );
    if (!permitido) {
      String aviso = URLDecoder.decode(
        requireNonNull(redireccion).substring(redireccion.indexOf("?aviso=") + 7),
        StandardCharsets.UTF_8
      );
      assertEquals(AVISO_MINIMO, aviso);
      assertThat(
        html(get("/dashboard").param("aviso", aviso), jugador),
        containsString(AVISO_MINIMO)
      );
    }
  }

  @Test
  public void elCapitanRecibeAvisoDeAceptacionSoloCuandoSeCompletaElPase() throws Exception {
    completarPlantel(sessionFactory.getCurrentSession().get(Equipo.class, origen.getId()), 6);
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    responder("aceptar", ofertaId, jugador);

    String aviso = html(get("/dashboard"), capitan);
    assertThat(aviso, containsString("aceptó tu oferta para SacaPuntos FC"));
    assertThat(aviso, containsString("jugadorOfertado"));
    assertThat(aviso, containsString(marcaRespuesta(ofertaId)));
    assertThat(aviso, containsString("id=\"respuestasTransferenciaCount\">1</span>"));
    assertThat(html(get("/dashboard"), jugador), not(containsString(marcaRespuesta(ofertaId))));
    assertThat(
      html(get("/dashboard"), origen.getOwner()),
      not(containsString(marcaRespuesta(ofertaId)))
    );
    assertTrue(pertenece(destino, jugador));
    assertFalse(pertenece(origen, jugador));
  }

  @Test
  public void elRechazoNotificaUnaVezYLeerElAvisoNoCambiaElPase() throws Exception {
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    responder("rechazar", ofertaId, jugador);

    assertThat(
      html(get("/dashboard"), capitan),
      containsString("rechazó tu oferta para SacaPuntos FC")
    );
    accionRespuesta("/transferencias/respuestas/leidas", ofertaId, capitan);
    responder("rechazar", ofertaId, jugador);
    responder("aceptar", ofertaId, jugador);
    assertThat(html(get("/dashboard"), capitan), not(containsString(marcaRespuesta(ofertaId))));
    assertEquals(EstadoInvitacionEquipo.RECHAZADA, oferta().getEstado());
    assertTrue(pertenece(origen, jugador));
    assertFalse(pertenece(destino, jugador));
  }

  @Test
  public void postergarAvisaSinCerrarLaOfertaYLuegoSePuedeAceptar() throws Exception {
    completarPlantel(sessionFactory.getCurrentSession().get(Equipo.class, origen.getId()), 6);
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    String detalle = html(get("/transferencias/ofertas/" + ofertaId), jugador);
    assertThat(detalle, containsString("action=\"/transferencias/ofertas/postergar\""));
    assertThat(detalle, containsString("El capitán recibirá un aviso"));

    accionRespuesta("/transferencias/ofertas/postergar", ofertaId, jugador);
    assertThat(
      html(get("/dashboard"), capitan),
      containsString("postergó la respuesta a tu oferta para SacaPuntos FC")
    );
    assertEquals(EstadoInvitacionEquipo.PENDIENTE, oferta().getEstado());
    assertTrue(pertenece(origen, jugador));
    assertFalse(pertenece(destino, jugador));
    assertThat(
      html(get("/transferencias/ofertas/" + ofertaId), jugador),
      containsString("Aceptar pase")
    );

    accionRespuesta("/transferencias/respuestas/leidas", ofertaId, capitan);
    accionRespuesta("/transferencias/ofertas/postergar", ofertaId, jugador);
    assertThat(html(get("/dashboard"), capitan), not(containsString(marcaRespuesta(ofertaId))));
    responder("aceptar", ofertaId, jugador);
    String avisoFinal = html(get("/dashboard"), capitan);
    assertThat(avisoFinal, containsString("aceptó tu oferta para SacaPuntos FC"));
    assertThat(avisoFinal, not(containsString("postergó la respuesta")));
    assertThat(avisoFinal, containsString("id=\"respuestasTransferenciaCount\">1</span>"));
  }

  @Test
  public void despuesDePostergarTambienPuedeRechazarYActualizarElAviso() throws Exception {
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    accionRespuesta("/transferencias/ofertas/postergar", ofertaId, jugador);
    responder("rechazar", ofertaId, jugador);
    accionRespuesta("/transferencias/ofertas/postergar", ofertaId, jugador);

    String aviso = html(get("/dashboard"), capitan);
    assertThat(aviso, containsString("rechazó tu oferta para SacaPuntos FC"));
    assertThat(aviso, not(containsString("postergó la respuesta")));
    assertEquals(EstadoInvitacionEquipo.RECHAZADA, oferta().getEstado());
  }

  @Test
  public void verLaOfertaOIntentarPostergarlaComoOtroUsuarioNoNotifica() throws Exception {
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    html(get("/transferencias/ofertas/" + ofertaId), jugador);
    accionRespuesta("/transferencias/ofertas/postergar", ofertaId, capitan);
    accionRespuesta("/transferencias/ofertas/postergar", 999999L, jugador);
    mockMvc
      .perform(post("/transferencias/ofertas/postergar").param("invitacionId", ofertaId.toString()))
      .andExpect(redirectedUrl("/login"));

    assertThat(html(get("/dashboard"), capitan), not(containsString(marcaRespuesta(ofertaId))));
    assertEquals(EstadoInvitacionEquipo.PENDIENTE, oferta().getEstado());
    assertTrue(pertenece(origen, jugador));
  }

  @Test
  public void soloElEmisorPuedeMarcarComoLeidaSuNotificacion() throws Exception {
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    accionRespuesta("/transferencias/ofertas/postergar", ofertaId, jugador);
    accionRespuesta("/transferencias/respuestas/leidas", ofertaId, jugador);
    mockMvc
      .perform(post("/transferencias/respuestas/leidas").param("invitacionId", ofertaId.toString()))
      .andExpect(redirectedUrl("/login"));

    assertThat(html(get("/dashboard"), capitan), containsString(marcaRespuesta(ofertaId)));
    accionRespuesta("/transferencias/respuestas/leidas", ofertaId, capitan);
    assertThat(html(get("/dashboard"), capitan), not(containsString(marcaRespuesta(ofertaId))));
    assertEquals(EstadoInvitacionEquipo.PENDIENTE, oferta().getEstado());
  }

  @Test
  public void unPaseBloqueadoPorMinimoORespuestaAjenaNoNotificaAceptacion() throws Exception {
    enviarOferta(capitan, origen, jugador);
    Long ofertaId = oferta().getId();
    responder("aceptar", ofertaId, jugador);
    responder("rechazar", ofertaId, capitan);

    assertThat(html(get("/dashboard"), capitan), not(containsString(marcaRespuesta(ofertaId))));
    assertEquals(EstadoInvitacionEquipo.PENDIENTE, oferta().getEstado());
  }

  @Test
  public void unaInvitacionDeCreacionNoGeneraAvisosDeTransferencia() throws Exception {
    InvitacionEquipo invitacion = new InvitacionEquipo();
    invitacion.setEquipo(sessionFactory.getCurrentSession().get(Equipo.class, destino.getId()));
    invitacion.setUsuario(sessionFactory.getCurrentSession().get(Usuario.class, jugador.getId()));
    sessionFactory.getCurrentSession().persist(invitacion);
    recargar();
    accionRespuesta("/transferencias/ofertas/postergar", invitacion.getId(), jugador);
    responder("aceptar", invitacion.getId(), jugador);

    assertEquals(EstadoInvitacionEquipo.ACEPTADA, oferta().getEstado());
    assertThat(
      html(get("/dashboard"), capitan),
      not(containsString(marcaRespuesta(invitacion.getId())))
    );
  }

  private void accionRespuesta(String ruta, Long ofertaId, Usuario usuario) throws Exception {
    mockMvc
      .perform(
        post(ruta)
          .sessionAttr("usuario", requireNonNull(usuario))
          .param("invitacionId", ofertaId.toString())
      )
      .andExpect(status().is3xxRedirection());
    recargar();
  }

  private static String marcaRespuesta(Long ofertaId) {
    return "data-respuesta-id=\"" + ofertaId + "\"";
  }

  private void completarPlantel(Equipo equipo, int cantidad) {
    while (equipo.getJugadores().size() < cantidad) {
      equipo.agregarJugador(
        usuario("suplente" + equipo.getId() + "_" + equipo.getJugadores().size())
      );
    }
  }

  private void inscribirEnCompetencia(String tipo, int formato, Equipo equipo) {
    if ("liga".equals(tipo)) {
      Liga liga = new Liga();
      liga.setNombre("Liga F" + formato);
      liga.setFormato(formato);
      sessionFactory.getCurrentSession().persist(liga);
      InscripcionLiga inscripcion = new InscripcionLiga();
      inscripcion.setLiga(liga);
      inscripcion.setEquipo(equipo);
      inscripcion.setUsuario(equipo.getOwner());
      sessionFactory.getCurrentSession().persist(inscripcion);
    } else {
      Torneo competencia = new Torneo();
      competencia.setNombre("Torneo F" + formato);
      competencia.setFormato(formato);
      sessionFactory.getCurrentSession().persist(competencia);
      InscripcionTorneo inscripcion = new InscripcionTorneo();
      inscripcion.setTorneo(competencia);
      inscripcion.setEquipo(equipo);
      inscripcion.setUsuario(equipo.getOwner());
      sessionFactory.getCurrentSession().persist(inscripcion);
    }
  }

  private String pantallaMercado(Usuario usuario, Equipo seleccionado, Usuario elegido)
    throws Exception {
    return html(
      get("/transferencias")
        .param("id", torneo.getId().toString())
        .param("equipo", seleccionado.getId().toString())
        .param("jugador", elegido.getId().toString()),
      usuario
    );
  }

  private String html(MockHttpServletRequestBuilder peticion, Usuario usuario) throws Exception {
    return mockMvc
      .perform(peticion.sessionAttr("usuario", requireNonNull(usuario)))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString(StandardCharsets.UTF_8);
  }

  private String enviarOferta(Usuario usuario, Equipo desde, Usuario elegido) throws Exception {
    String url = mockMvc
      .perform(
        post("/transferencias/ofertar")
          .sessionAttr("usuario", requireNonNull(usuario))
          .param("tipo", "torneo")
          .param("id", torneo.getId().toString())
          .param("equipo", desde.getId().toString())
          .param("jugador", elegido.getId().toString())
      )
      .andExpect(status().is3xxRedirection())
      .andReturn()
      .getResponse()
      .getRedirectedUrl();
    recargar();
    return url;
  }

  private String responder(String accion, Long ofertaId, Usuario usuario) throws Exception {
    String redireccion = mockMvc
      .perform(
        post("/equipos/invitaciones/" + accion)
          .sessionAttr("usuario", requireNonNull(usuario))
          .param("invitacionId", ofertaId.toString())
      )
      .andExpect(status().is3xxRedirection())
      .andReturn()
      .getResponse()
      .getRedirectedUrl();
    recargar();
    return redireccion;
  }

  private Usuario usuario(String nombre) {
    Usuario usuario = new Usuario();
    usuario.setEmail(nombre + "@test.com");
    usuario.setRol("USER");
    usuario.getPerfil().setNombreUsuario(nombre);
    usuario.getPerfil().setNombreCompleto(nombre);
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Equipo equipo(String nombre, Usuario owner) {
    Equipo equipo = new Equipo();
    equipo.setNombre(nombre);
    equipo.setFormato(5);
    equipo.setEstado(EstadoEquipo.ACTIVO);
    equipo.setOwner(owner);
    equipo.agregarJugador(owner);
    sessionFactory.getCurrentSession().persist(equipo);
    return equipo;
  }

  private void inscribir(Equipo equipo) {
    InscripcionTorneo inscripcion = new InscripcionTorneo();
    inscripcion.setEquipo(equipo);
    inscripcion.setUsuario(equipo.getOwner());
    inscripcion.setTorneo(torneo);
    sessionFactory.getCurrentSession().persist(inscripcion);
  }

  private boolean pertenece(Equipo equipo, Usuario usuario) {
    return sessionFactory
      .getCurrentSession()
      .get(Equipo.class, equipo.getId())
      .getJugadores()
      .stream()
      .anyMatch(j -> j.getId().equals(usuario.getId()));
  }

  private InvitacionEquipo oferta() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from InvitacionEquipo", InvitacionEquipo.class)
      .getSingleResult();
  }

  private long cantidadOfertas() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("select count(i) from InvitacionEquipo i", Long.class)
      .getSingleResult();
  }

  private void recargar() {
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
  }
}
