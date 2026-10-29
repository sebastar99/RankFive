import { readFileSync } from "node:fs";
import { JSDOM } from "jsdom";

const html = readFileSync(new URL("../../../../WEB-INF/views/thymeleaf/chat.html", import.meta.url));
const script = readFileSync(new URL("../chat.js", import.meta.url));

describe("Chat: textos y caracteres disponibles", function() {
  let dom;
  let document;
  let socket;
  let input;
  let form;
  let sendSpy;

  beforeEach(function() {
    sendSpy = jasmine.createSpy("send");
  });

  async function preparar(codificacion = "utf8") {
    dom = new JSDOM(html.toString(codificacion), {
      url: "http://localhost/spring/chat/2",
      runScripts: "outside-only"
    });
    const window = dom.window;
    document = window.document;
    Object.assign(document.getElementById("chat").dataset, {
      usuarioActualId: "1",
      otroUsuarioId: "2",
      otroUsuarioNombre: "Amigo",
      historialUrl: "/spring/chat/2/mensajes",
      websocketUrl: "/spring/ws/chat"
    });
    window.fetch = async () => ({ ok: true, json: async () => [] });
    window.WebSocket = class extends window.EventTarget {
      static OPEN = 1;
      static CLOSED = 3;

      constructor() {
        super();
        this.readyState = 1;
        this.send = sendSpy;
        socket = this;
      }
    };
    window.eval(script.toString(codificacion));
    socket.dispatchEvent(new window.Event("open"));
    await new Promise(resolve => setTimeout(resolve, 0));
    input = document.getElementById("contenido");
    form = document.getElementById("formulario-chat");
  }

  function escribir(texto) {
    input.value = texto;
    input.dispatchEvent(new dom.window.Event("input", { bubbles: true }));
  }

  function contador() {
    return document.getElementById("contador-caracteres")?.textContent.trim();
  }

  function enviar() {
    form.dispatchEvent(new dom.window.Event("submit", { cancelable: true }));
  }

  afterEach(function() {
    dom?.window.close();
  });

  it("empieza con 2000 caracteres disponibles", async function() {
    await preparar();

    expect(contador()).toBe("2000 caracteres disponibles");
  });

  it("descuenta al escribir y recupera al borrar", async function() {
    await preparar();
    escribir("Hola");

    expect(contador()).toBe("1996 caracteres disponibles");
    escribir("Ho");

    expect(contador()).toBe("1998 caracteres disponibles");
    escribir("");

    expect(contador()).toBe("2000 caracteres disponibles");
  });

  it("muestra el limite al pegar 2000 caracteres", async function() {
    await preparar();
    escribir("a".repeat(1999));

    expect(contador()).toBe("1 car\u00e1cter disponible");
    escribir("a".repeat(2000));

    expect(contador()).toBe("0 caracteres disponibles");
    expect(document.getElementById("contador-caracteres").classList.contains("chat-counter-limit")).toBeTrue();
    escribir("a");

    expect(document.getElementById("contador-caracteres").classList.contains("chat-counter-limit")).toBeFalse();
  });

  it("cuenta espacios, saltos y emojis segun maxlength del textarea", async function() {
    await preparar();
    escribir("a \n\u{1F600}");

    expect(contador()).toBe("1995 caracteres disponibles");
  });

  it("reinicia el contador al enviar", async function() {
    await preparar();
    escribir("Hola");
    enviar();

    expect(socket.send).toHaveBeenCalledWith(JSON.stringify({ destinatarioId: 2, contenido: "Hola" }));
    expect(input.value).toBe("");
    expect(contador()).toBe("2000 caracteres disponibles");
  });

  it("actualiza el contador si el servidor restaura el mensaje rechazado", async function() {
    await preparar();
    escribir("Hola");
    enviar();
    socket.dispatchEvent(new dom.window.MessageEvent("message", {
      data: JSON.stringify({ tipo: "error", descripcion: "No se pudo guardar." })
    }));

    expect(input.value).toBe("Hola");
    expect(contador()).toBe("1996 caracteres disponibles");
  });

  it("conserva borrador y contador si el envio falla", async function() {
    await preparar();
    socket.send.and.throwError("Sin conexion");
    escribir("Hola");
    enviar();

    expect(input.value).toBe("Hola");
    expect(contador()).toBe("1996 caracteres disponibles");
  });

  it("muestra las tildes incluso si los recursos se decodifican como latin1", async function() {
    await preparar("latin1");

    expect(document.querySelector(".chat-eyebrow").textContent).toBe("Conversaci\u00f3n directa");
    expect(document.querySelector("#lista-mensajes > p").textContent.trim())
      .toBe("Todav\u00eda no hay mensajes en esta conversaci\u00f3n.");
  });
});
