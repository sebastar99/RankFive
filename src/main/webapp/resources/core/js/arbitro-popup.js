(() => {
  "use strict";

  const perfiles = new Map([
    ["hector baldassi", {
      pl: 3400,
      puntos: 46,
      votos: 10,
      foto: "https://upload.wikimedia.org/wikipedia/commons/6/6c/Hector_Baldassi%2C_2022_%28cropped%29.jpg",
      carrera: "Dirigi\u00f3 en el Mundial de Sud\u00e1frica 2010. Fue uno de los \u00e1rbitros argentinos que representaron al pa\u00eds en la m\u00e1xima competencia de selecciones."
    }],

    ["nestor pitana", {
      pl: 3600,
      puntos: 44,
      votos: 10,
      foto: "https://cdn.conmebol.com/wp-content/uploads/2014/09/pitana-620px.jpg",
      carrera: "Particip\u00f3 en Brasil 2014 y Rusia 2018. En Rusia dirigi\u00f3 tanto el partido inaugural como la final entre Francia y Croacia."
    }],

    ["patricio loustau", {
      pl: 3100,
      puntos: 39,
      votos: 10,
      foto: "https://media.tycsports.com/files/2022/11/11/503945/patricio-loustau_1440x810_wmk.webp?v=1",
      carrera: "Ex\u00e1rbitro internacional argentino. Dirigi\u00f3 la final de la Copa Libertadores 2022 entre Flamengo y Athletico Paranaense y se retir\u00f3 ese a\u00f1o."
    }],

    ["fernando rapallini", {
      pl: 2900,
      puntos: 33,
      votos: 8,
      foto: "https://provinciaradio.com.ar/images/IMG-88fc07e8-f77a-4937-b768-0339a55d0a23.jpg",
      carrera: "Fue el primer \u00e1rbitro sudamericano que particip\u00f3 en una Eurocopa, en la edici\u00f3n 2020 disputada en 2021. Tambi\u00e9n dirigi\u00f3 en Qatar 2022."
    }],

    ["dario herrera", {
      pl: 2500,
      puntos: 21,
      votos: 6,
      foto: "https://cloudfront-us-east-1.images.arcpublishing.com/infobae/T4UJBB2CXVHBZFGOURCGFCXW2I.jpg",
      credito: "Hernan Cortez / Getty Images, v\u00eda Infobae",
      carrera: "Lleg\u00f3 a Primera Divisi\u00f3n en 2013 y a la categor\u00eda FIFA en 2015. Dirigi\u00f3 Supercl\u00e1sicos y la final de la Copa de la Liga 2022 entre Boca y Tigre."
    }],

    ["facundo tello", {
      pl: 2700,
      puntos: 28,
      votos: 7,
      foto: "https://www.afa.com.ar/upload/banners/tellovert.jpg",
      carrera: "Represent\u00f3 al arbitraje argentino en Qatar 2022. Estuvo a cargo del cruce entre Marruecos y Portugal por los cuartos de final."
    }],

    ["yael falcon perez", {
      pl: 2200,
      puntos: 15,
      votos: 5,
      foto: "https://assets1.afa.com.ar/torneo/Lea/Julio/FINAL/Captura-de-pantalla-2024-07-22-112931.png",
      carrera: "\u00c1rbitro internacional desde 2022. Fue designado para dirigir Francia contra Estados Unidos en el torneo masculino de los Juegos Ol\u00edmpicos de Par\u00eds 2024."
    }],

    ["andres merlos", {
      pl: 1800,
      puntos: 10,
      votos: 4,
      foto: "https://media.tycsports.com/files/2024/09/02/761752/andres-merlos_1440x810_wmk.webp?v=1",
      carrera: "\u00c1rbitro del f\u00fatbol argentino con experiencia en Primera Divisi\u00f3n. Fue designado para el Sudamericano Sub-17 de 2023 y el Supercl\u00e1sico de octubre de ese a\u00f1o."
    }]
  ]);

  function normalizarNombre(nombre) {
    return nombre
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .trim()
      .replace(/\s+/g, " ")
      .toLowerCase();
  }

  const popup = document.createElement("dialog");

  popup.className = "arbitro-popup";
  popup.setAttribute("aria-labelledby", "arbitro-popup-nombre");

  popup.innerHTML = `
    <div class="arbitro-popup-cabecera">
      <div>
        <span class="arbitro-popup-etiqueta">
          Perfil del \u00e1rbitro
        </span>

        <h2 id="arbitro-popup-nombre"></h2>
      </div>

      <button
        type="button"
        class="arbitro-popup-cerrar"
        aria-label="Cerrar perfil"
        autofocus>
        &times;
      </button>
    </div>

    <figure class="arbitro-popup-retrato">
      <img
        class="arbitro-popup-foto"
        alt=""
        hidden>

      <p class="arbitro-popup-sin-foto" hidden>
        Foto no disponible
      </p>
    </figure>

    <section class="arbitro-popup-trayectoria">
      <h3>Trayectoria profesional</h3>
      <p class="arbitro-popup-bio"></p>
    </section>

    <dl>
      <div class="arbitro-popup-dato">
        <dt>PL</dt>
        <dd data-campo="pl"></dd>
      </div>

      <div class="arbitro-popup-dato">
        <dt>Calificaci\u00f3n</dt>
        <dd data-campo="promedio"></dd>
      </div>

      <div class="arbitro-popup-dato">
        <dt>Calificaciones recibidas</dt>
        <dd data-campo="votos"></dd>
      </div>
    </dl>

  `;

  document.body.appendChild(popup);

  const foto = popup.querySelector(".arbitro-popup-foto");
  const sinFoto = popup.querySelector(".arbitro-popup-sin-foto");
  const biografia = popup.querySelector(".arbitro-popup-bio");

  let disparador;

  function mostrar(campo, valor) {
    popup.querySelector(
      `[data-campo="${campo}"]`
    ).textContent = valor;
  }

  // Si la imagen no carga, mostrar un aviso.
  foto.addEventListener("error", () => {
    foto.hidden = true;
    sinFoto.hidden = false;
  });

  document.addEventListener("click", (event) => {
    const boton = event.target.closest("[data-arbitro-nombre]");

    if (!boton || popup.open) {
      return;
    }

    disparador = boton;

    const nombre = boton.dataset.arbitroNombre.trim();
    const perfil = perfiles.get(normalizarNombre(nombre));

    popup.querySelector("h2").textContent = nombre;

    // Limpiar la foto del perfil anterior.
    foto.hidden = true;
    foto.removeAttribute("src");
    sinFoto.hidden = true;

    if (perfil) {
      foto.alt = "Foto de " + nombre;
      foto.src = perfil.foto;
      foto.hidden = false;

      biografia.textContent = perfil.carrera;

      mostrar("pl", perfil.pl);
      mostrar("votos", perfil.votos);

      const promedio = perfil.puntos / perfil.votos;

      mostrar(
        "promedio",
        promedio.toLocaleString("es-AR", {
          minimumFractionDigits: 1,
          maximumFractionDigits: 1
        }) + " / 5"
      );
    } else {
      sinFoto.hidden = false;

      biografia.textContent =
        "No hay una trayectoria cargada para este perfil.";

      mostrar("pl", "Sin datos");
      mostrar("votos", "Sin datos");
      mostrar("promedio", "Sin datos");
    }

    popup.showModal();
  });

  popup.querySelector(".arbitro-popup-cerrar")
    .addEventListener("click", () => {
      popup.close();
    });

  popup.addEventListener("click", (event) => {
    const rect = popup.getBoundingClientRect();

    const clicAfuera =
      event.clientX < rect.left ||
      event.clientX > rect.right ||
      event.clientY < rect.top ||
      event.clientY > rect.bottom;

    if (event.target === popup && clicAfuera) {
      popup.close();
    }
  });

  popup.addEventListener("close", () => {
    disparador?.focus();
  });
})();