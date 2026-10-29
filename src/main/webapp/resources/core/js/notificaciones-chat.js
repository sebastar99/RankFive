"use strict";

(() => {
  const panel = document.getElementById("avisos-chat");

  if (!panel || panel.dataset.iniciado === "true") {
    return;
  }

  panel.dataset.iniciado = "true";

  const lista = document.getElementById("lista-mensajes-chat");
  const cantidad = document.getElementById("cantidad-mensajes-chat");
  const vacio = document.getElementById("sin-mensajes-chat");
  const indicador = document.getElementById("indicador-mensajes-chat");

  const usuarioId = panel.dataset.usuarioId;
  const recibidos = new Set();

  let socket;
  let temporizador;
  let detenido = false;
  let demora = 1000;

  function mostrarNotificacion(mensaje) {
    // Solo se notifica al destinatario.
    if (String(mensaje.destinatarioId) !== usuarioId) {
      return;
    }

    const id = String(mensaje.id);

    if (recibidos.has(id)) {
      return;
    }

    recibidos.add(id);

    if (recibidos.size > 500) {
      recibidos.delete(recibidos.values().next().value);
    }

    const nombre = mensaje.emisorNombre
            || `Usuario #${mensaje.emisorId}`;

    const aviso = document.createElement("div");
    aviso.className = "notif-item";

    const avatar = document.createElement("div");
    avatar.className = "notif-avatar";

    const icono = document.createElement("i");
    icono.className = "bi bi-chat-dots-fill";
    avatar.appendChild(icono);

    const informacion = document.createElement("div");
    informacion.className = "notif-info";
    informacion.style.minWidth = "0";

    const titulo = document.createElement("div");
    titulo.className = "notif-name";
    titulo.textContent = `Nuevo mensaje de ${nombre}`;

    informacion.appendChild(titulo);

    const acciones = document.createElement("div");
    acciones.className = "notif-actions";

    const enlace = document.createElement("a");
    enlace.className = "btn btn-sm btn-outline-light";
    enlace.textContent = "Ver";
    enlace.href = panel.dataset.chatBaseUrl
            + encodeURIComponent(String(mensaje.emisorId));

    acciones.appendChild(enlace);
    aviso.append(avatar, informacion, acciones);

    // Los avisos más recientes aparecen primero.
    lista.prepend(aviso);

    const total = Number(cantidad.textContent) + 1;

    cantidad.textContent = String(total);
    indicador.textContent = `(${total})`;
    indicador.hidden = false;
    vacio.hidden = true;

    // Limitamos los elementos visibles para no sobrecargar la página.
    while (lista.children.length > 50) {
      lista.lastElementChild.remove();
    }
  }

  function conectar() {
    if (detenido) {
      return;
    }

    const url = new URL(
      panel.dataset.websocketUrl,
      window.location.href
    );

    url.protocol = url.protocol === "https:" ? "wss:" : "ws:";

    socket = new WebSocket(url.href);

    socket.addEventListener("open", () => {
      demora = 1000;
    });

    socket.addEventListener("message", (evento) => {
      let respuesta;

      try {
        respuesta = JSON.parse(evento.data);
      } catch {
        return;
      }

      if (respuesta.tipo === "mensaje" && respuesta.datos) {
        mostrarNotificacion(respuesta.datos);
      }
    });

    socket.addEventListener("close", (evento) => {
      // No reconectamos si la sesión fue rechazada.
      if (detenido || evento.code === 1008) {
        return;
      }

      temporizador = window.setTimeout(conectar, demora);
      demora = Math.min(demora * 2, 30000);
    });
  }

  window.addEventListener("pagehide", () => {
    detenido = true;
    window.clearTimeout(temporizador);

    if (socket) {
      socket.close();
    }
  });

  window.addEventListener("pageshow", (evento) => {
    if (evento.persisted) {
      window.location.reload();
    }
  });

  conectar();
})();