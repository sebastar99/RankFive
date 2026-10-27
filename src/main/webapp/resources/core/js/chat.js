"use strict";

const chat = document.getElementById("chat");

const usuarioActualId = chat.dataset.usuarioActualId;
const otroUsuarioId = chat.dataset.otroUsuarioId;
const otroUsuarioNombre = chat.dataset.otroUsuarioNombre;

const listaMensajes = document.getElementById("lista-mensajes");
const formulario = document.getElementById("formulario-chat");
const camposChat = document.getElementById("campos-chat");
const contenido = document.getElementById("contenido");
const estadoConexion = document.getElementById("estado-conexion");
const errorChat = document.getElementById("error-chat");
const botonReconectar = document.getElementById("boton-reconectar");

const mensajes = new Map();

let socket;
let historialPreparado = false;
let ultimoTextoEnviado = "";

function mostrarError(texto) {
    errorChat.textContent = texto;
    errorChat.hidden = false;
}

function limpiarError() {
    errorChat.textContent = "";
    errorChat.hidden = true;
}

function perteneceALaConversacion(mensaje) {
    const emisor = String(mensaje.emisorId);
    const destinatario = String(mensaje.destinatarioId);

    return (
        (emisor === usuarioActualId && destinatario === otroUsuarioId) ||
        (emisor === otroUsuarioId && destinatario === usuarioActualId)
    );
}

function incorporarMensaje(mensaje) {
    if (perteneceALaConversacion(mensaje)) {
        mensajes.set(String(mensaje.id), mensaje);
    }
}

function mostrarMensajes() {
    const estabaAbajo =
        listaMensajes.scrollHeight -
        listaMensajes.scrollTop -
        listaMensajes.clientHeight < 80;

    const ordenados = Array.from(mensajes.values()).sort((a, b) => {
        const diferenciaFecha =
            a.fechaEnvio.localeCompare(b.fechaEnvio);

        if (diferenciaFecha !== 0) {
            return diferenciaFecha;
        }

        return Number(a.id) - Number(b.id);
    });

    const fragmento = document.createDocumentFragment();

    if (ordenados.length === 0) {
        const aviso = document.createElement("p");
        aviso.className = "text-center text-muted py-5";
        aviso.textContent =
            "Todavía no hay mensajes en esta conversación.";

        fragmento.appendChild(aviso);
    }

    for (const mensaje of ordenados) {
        const esMio = String(mensaje.emisorId) === usuarioActualId;

        const articulo = document.createElement("article");
        articulo.className = esMio
            ? "d-flex mb-3 justify-content-end"
            : "d-flex mb-3 justify-content-start";
        articulo.dataset.mensajeId = String(mensaje.id);

        const burbuja = document.createElement("div");
        burbuja.className = esMio
            ? "rounded p-3 bg-primary text-white"
            : "rounded p-3 bg-light border";
        burbuja.style.maxWidth = "80%";
        burbuja.style.overflowWrap = "anywhere";

        const autor = document.createElement("p");
        autor.className = "fw-bold small mb-1";
autor.textContent = esMio
    ? "Vos"
    : otroUsuarioNombre;

        const texto = document.createElement("p");
        texto.className = "mb-2";
        texto.style.whiteSpace = "pre-wrap";
        texto.textContent = mensaje.contenido;

        const fecha = document.createElement("time");
        fecha.className = "small";
        fecha.dateTime = mensaje.fechaEnvio;
        fecha.textContent = mensaje.fechaEnvio.replace("T", " ");

        burbuja.append(autor, texto, fecha);
        articulo.appendChild(burbuja);
        fragmento.appendChild(articulo);
    }

    listaMensajes.replaceChildren(fragmento);

    if (estabaAbajo) {
        listaMensajes.scrollTop = listaMensajes.scrollHeight;
    }
}

async function cargarHistorial() {
    const respuesta = await fetch(chat.dataset.historialUrl, {
        headers: {
            Accept: "application/json"
        },
        cache: "no-store"
    });

    if (respuesta.status === 401) {
        throw new Error(
            "Tu sesión terminó. Volvé a iniciar sesión."
        );
    }

    if (!respuesta.ok) {
        throw new Error("No se pudo recuperar el historial.");
    }

    const historial = await respuesta.json();

    if (!Array.isArray(historial)) {
        throw new Error("El historial recibido no es válido.");
    }

    for (const mensaje of historial) {
        incorporarMensaje(mensaje);
    }
}

function conectar() {
    if (
        socket &&
        socket.readyState !== WebSocket.CLOSED
    ) {
        return;
    }

    limpiarError();
    camposChat.disabled = true;
    historialPreparado = false;
    botonReconectar.hidden = true;
    estadoConexion.textContent = "Conectando...";

    const url = new URL(
        chat.dataset.websocketUrl,
        window.location.href
    );

    url.protocol = window.location.protocol === "https:"
        ? "wss:"
        : "ws:";

    socket = new WebSocket(url.href);

    socket.addEventListener("open", async () => {
        estadoConexion.textContent = "Recuperando historial...";

        try {
            await cargarHistorial();

            if (socket.readyState !== WebSocket.OPEN) {
                return;
            }

            historialPreparado = true;
            mostrarMensajes();

            listaMensajes.scrollTop = listaMensajes.scrollHeight;

            camposChat.disabled = false;
            estadoConexion.textContent = "Conectado al servidor";
        } catch (error) {
            mostrarError(error.message);
            socket.close();
        }
    });

    socket.addEventListener("message", (evento) => {
        let respuesta;

        try {
            respuesta = JSON.parse(evento.data);
        } catch {
            mostrarError("Se recibió una respuesta no válida.");
            return;
        }

        if (respuesta.tipo === "error") {
            mostrarError(respuesta.descripcion);

            if (contenido.value === "" && ultimoTextoEnviado !== "") {
                contenido.value = ultimoTextoEnviado;
            }

            return;
        }

        if (respuesta.tipo === "mensaje") {
            incorporarMensaje(respuesta.datos);

            if (historialPreparado) {
                mostrarMensajes();
            }
        }
    });

    socket.addEventListener("close", () => {
        camposChat.disabled = true;
        historialPreparado = false;
        botonReconectar.hidden = false;
        estadoConexion.textContent = "Desconectado";
    });

    socket.addEventListener("error", () => {
        mostrarError(
            "No se pudo mantener la conexión. Si tu sesión terminó, volvé a iniciar sesión."
        );
    });
}

formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    limpiarError();

    const texto = contenido.value.trim();

    if (texto === "") {
        mostrarError("Escribí un mensaje antes de enviar.");
        return;
    }

    if (texto.length > 2000) {
        mostrarError("El mensaje no puede superar los 2000 caracteres.");
        return;
    }

    if (
        !socket ||
        socket.readyState !== WebSocket.OPEN ||
        !historialPreparado
    ) {
        mostrarError("Esperá a que se restablezca la conexión.");
        return;
    }

    try {
        socket.send(JSON.stringify({
            destinatarioId: Number(otroUsuarioId),
            contenido: texto
        }));

        ultimoTextoEnviado = texto;
        contenido.value = "";
        contenido.focus();
    } catch {
        mostrarError(
            "No se pudo enviar el mensaje. Revisá la conexión."
        );
    }
});

botonReconectar.addEventListener("click", conectar);

conectar();