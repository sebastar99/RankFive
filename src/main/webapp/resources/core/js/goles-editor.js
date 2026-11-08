// Editor de goles/tarjetas: agrega y quita filas dentro de cada .gol-section.
// Lo usan partidos (amistosos), ligas y torneos.
(function () {
  const renumerar = (rows) => {
    rows.querySelectorAll(".gol-num").forEach((n, i) => {
      n.textContent = i + 1;
    });
  };

  document.querySelectorAll("[data-gol-add]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const rows = btn.closest(".gol-section")?.querySelector(".gol-rows");
      const primera = rows?.querySelector(".gol-row");
      if (!primera) {
        return;
      }
      const clone = primera.cloneNode(true);
      clone.querySelectorAll("select").forEach((s) => {
        s.selectedIndex = 0;
      });
      rows.appendChild(clone);
      renumerar(rows);
    });
  });

  document.querySelectorAll(".gol-rows").forEach((rows) => {
    rows.addEventListener("click", (e) => {
      const quitar = e.target.closest("[data-gol-remove]");
      if (!quitar || rows.querySelectorAll(".gol-row").length <= 1) {
        return;
      }
      quitar.closest(".gol-row").remove();
      renumerar(rows);
    });
  });
})();
