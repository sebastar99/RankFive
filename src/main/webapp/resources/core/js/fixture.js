// Slider de fechas/fases del fixture (liga y torneo).
// Muestra un panel a la vez y permite navegar con flechas o con la tira de pills.
document.addEventListener("DOMContentLoaded", () => {
  const fixture = document.querySelector(".fixture");
  if (!fixture) {
    return;
  }
  const pills = Array.from(fixture.querySelectorAll(".fecha-pill"));
  const paneles = Array.from(fixture.querySelectorAll(".fecha-panel"));
  const flechas = fixture.querySelectorAll("[data-fixture-mover]");
  const strip = fixture.querySelector(".fecha-strip");

  const indiceActivo = () => {
    const i = pills.findIndex((p) => p.classList.contains("activa"));
    return i < 0 ? 0 : i;
  };

  const mostrar = (indice) => {
    if (indice < 0 || indice >= pills.length) {
      return;
    }
    const pill = pills[indice];
    const fecha = pill.dataset.fecha;
    pills.forEach((p) => p.classList.toggle("activa", p.dataset.fecha === fecha));
    paneles.forEach((p) => p.classList.toggle("activa", p.dataset.fecha === fecha));
    const izquierda = pill.offsetLeft - strip.offsetLeft - (strip.clientWidth - pill.offsetWidth) / 2;
    strip.scrollTo({ left: izquierda, behavior: "smooth" });
    flechas[0].disabled = indice === 0;
    flechas[1].disabled = indice === pills.length - 1;
  };

  pills.forEach((p, i) => p.addEventListener("click", () => mostrar(i)));
  flechas.forEach((b) => {
    b.addEventListener("click", () => mostrar(indiceActivo() + Number(b.dataset.fixtureMover)));
  });
  mostrar(indiceActivo());
});
