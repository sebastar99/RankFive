package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.Liga;
import com.tallerwebi.dominio.RepositorioLiga;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HibernateInfraestructuraTestConfig.class)
@Transactional
public class RepositorioLigaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioLiga repositorioLiga;

  @BeforeEach
  public void init() {
    repositorioLiga = new RepositorioLigaImpl(sessionFactory);
  }

  @Test
  public void guardarYBuscarLiga() {
    Liga l = new Liga();
    l.setNombre("Liga Test");
    l.setDescripcion("Desc");
    l.setFormato(5);
    l.setCupoEquipos(20);
    l.setFechaInicio(LocalDate.now());
    l.setUbicacion("CABA");

    repositorioLiga.guardar(l);
    sessionFactory.getCurrentSession().flush();

    Liga recuperada = repositorioLiga.buscarPorId(l.getId());
    assertThat(recuperada, notNullValue());
    assertThat(recuperada.getNombre(), equalTo("Liga Test"));
  }

  @Test
  public void listarTodosOrdenadosPorFecha() {
    Liga a = new Liga();
    a.setNombre("A");
    a.setFormato(5);
    a.setCupoEquipos(10);
    a.setFechaInicio(LocalDate.of(2024, 1, 1));
    a.setUbicacion("X");
    repositorioLiga.guardar(a);

    Liga b = new Liga();
    b.setNombre("B");
    b.setFormato(5);
    b.setCupoEquipos(10);
    b.setFechaInicio(LocalDate.of(2025, 1, 1));
    b.setUbicacion("Y");
    repositorioLiga.guardar(b);

    sessionFactory.getCurrentSession().flush();

    List<Liga> todas = repositorioLiga.listarTodos();
    assertThat(todas, hasSize(2));
    assertThat(todas.get(0).getNombre(), equalTo("B"));
  }
}
