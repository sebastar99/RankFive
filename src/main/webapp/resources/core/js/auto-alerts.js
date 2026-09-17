// Cierra automaticamente los alerts marcados con la clase .alert-auto
// Requiere Bootstrap bundle cargado previamente.
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.alert.alert-auto').forEach((el) => {
    window.setTimeout(() => {
      bootstrap.Alert.getOrCreateInstance(el).close();
    }, 4000);
  });
});
