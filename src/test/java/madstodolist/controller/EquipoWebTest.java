package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import java.util.Arrays;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipoService equipoService;

    @MockBean
    private ManagerUserSession managerUserSession;

    private EquipoData equipo(Long id, String nombre) {
        EquipoData e = new EquipoData();
        e.setId(id);
        e.setNombre(nombre);
        return e;
    }

    @Test
    public void listadoEquiposMuestraNombres() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(equipoService.findAllOrdenadoPorNombre())
                .thenReturn(Arrays.asList(equipo(1L, "Proyecto AAA"), equipo(2L, "Proyecto BBB")));

        this.mockMvc.perform(get("/equipos"))
                .andExpect(content().string(allOf(
                        containsString("Proyecto AAA"),
                        containsString("Proyecto BBB"))));
    }

    @Test
    public void listadoEquiposSinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void miembrosEquipoMuestraUsuarios() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        UsuarioData ana = new UsuarioData();
        ana.setId(1L);
        ana.setEmail("ana@ua");
        when(equipoService.recuperarEquipo(5L)).thenReturn(equipo(5L, "Proyecto AAA"));
        when(equipoService.usuariosEquipo(5L)).thenReturn(Arrays.asList(ana));

        this.mockMvc.perform(get("/equipos/5"))
                .andExpect(content().string(allOf(
                        containsString("Proyecto AAA"),
                        containsString("ana@ua"))));
    }

    @Test
    public void miembrosEquipoInexistenteDevuelve404() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(equipoService.recuperarEquipo(50L))
                .thenThrow(new EquipoServiceException("Equipo 50 no existe"));

        this.mockMvc.perform(get("/equipos/50"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void miembrosEquipoSinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/equipos/5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getNuevoEquipoDevuelveFormulario() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        this.mockMvc.perform(get("/equipos/nuevo"))
                .andExpect(content().string(containsString("action=\"/equipos/nuevo\"")));
    }

    @Test
    public void postNuevoEquipoRedirigeAlListado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        this.mockMvc.perform(post("/equipos/nuevo").param("nombre", "Proyecto X"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).crearEquipo("Proyecto X");
    }

    @Test
    public void postNuevoEquipoVacioMuestraError() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(equipoService.crearEquipo(""))
                .thenThrow(new EquipoServiceException("El nombre del equipo no puede estar vacío"));

        this.mockMvc.perform(post("/equipos/nuevo").param("nombre", ""))
                .andExpect(content().string(containsString("El nombre del equipo no puede estar vacío")));
    }

    @Test
    public void nuevoEquipoSinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(post("/equipos/nuevo").param("nombre", "Proyecto X"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void paginaEquipoMuestraUnirmeSiNoPertenece() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(equipoService.recuperarEquipo(5L)).thenReturn(equipo(5L, "Proyecto AAA"));
        when(equipoService.usuariosEquipo(5L)).thenReturn(Arrays.asList());
        when(equipoService.usuarioPerteneceAEquipo(5L, 1L)).thenReturn(false);

        this.mockMvc.perform(get("/equipos/5"))
                .andExpect(content().string(allOf(
                        containsString("/equipos/5/unirse"),
                        not(containsString("/equipos/5/salir")))));
    }

    @Test
    public void paginaEquipoMuestraSalirSiPertenece() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(equipoService.recuperarEquipo(5L)).thenReturn(equipo(5L, "Proyecto AAA"));
        when(equipoService.usuariosEquipo(5L)).thenReturn(Arrays.asList());
        when(equipoService.usuarioPerteneceAEquipo(5L, 1L)).thenReturn(true);

        this.mockMvc.perform(get("/equipos/5"))
                .andExpect(content().string(allOf(
                        containsString("/equipos/5/salir"),
                        not(containsString("/equipos/5/unirse")))));
    }

    @Test
    public void postUnirseAñadeUsuarioYRedirige() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        this.mockMvc.perform(post("/equipos/5/unirse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/5"));

        verify(equipoService).añadirUsuarioAEquipo(5L, 1L);
    }

    @Test
    public void postSalirEliminaUsuarioYRedirige() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        this.mockMvc.perform(post("/equipos/5/salir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/5"));

        verify(equipoService).eliminarUsuarioDeEquipo(5L, 1L);
    }

    @Test
    public void postUnirseSiYaPerteneceMuestraError() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        doThrow(new EquipoServiceException("El usuario 1 ya pertenece al equipo 5"))
                .when(equipoService).añadirUsuarioAEquipo(5L, 1L);

        this.mockMvc.perform(post("/equipos/5/unirse"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "El usuario 1 ya pertenece al equipo 5"));
    }

    @Test
    public void unirseYSalirSinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(post("/equipos/5/unirse"))
                .andExpect(status().isUnauthorized());
        this.mockMvc.perform(post("/equipos/5/salir"))
                .andExpect(status().isUnauthorized());
    }

    @MockBean
    private UsuarioService usuarioService;

    // Simula un usuario logeado, administrador o no
    private void logearComo(Long id, boolean administrador) {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(id);
        usuario.setAdministrador(administrador);
        when(managerUserSession.usuarioLogeado()).thenReturn(id);
        when(usuarioService.findById(id)).thenReturn(usuario);
    }
    @Test
    public void adminVeFormularioEditarConNombreActual() throws Exception {
        logearComo(99L, true);
        when(equipoService.recuperarEquipo(5L)).thenReturn(equipo(5L, "Proyecto AAA"));

        this.mockMvc.perform(get("/equipos/5/editar"))
                .andExpect(content().string(allOf(
                        containsString("action=\"/equipos/5/editar\""),
                        containsString("Proyecto AAA"))));
    }

    @Test
    public void adminEditaEquipoYRedirige() throws Exception {
        logearComo(99L, true);

        this.mockMvc.perform(post("/equipos/5/editar").param("nombre", "Proyecto Nuevo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/5"));

        verify(equipoService).renombrarEquipo(5L, "Proyecto Nuevo");
    }

    @Test
    public void adminEditaConNombreVacioVuelveAlFormularioConError() throws Exception {
        logearComo(99L, true);
        when(equipoService.renombrarEquipo(5L, ""))
                .thenThrow(new EquipoServiceException("El nombre del equipo no puede estar vacío"));

        this.mockMvc.perform(post("/equipos/5/editar").param("nombre", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/5/editar"))
                .andExpect(flash().attribute("error", "El nombre del equipo no puede estar vacío"));
    }

    @Test
    public void editarEquipoInexistenteDevuelve404() throws Exception {
        logearComo(99L, true);
        when(equipoService.recuperarEquipo(50L))
                .thenThrow(new EquipoServiceException("Equipo 50 no existe"));

        this.mockMvc.perform(get("/equipos/50/editar"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void usuarioNormalNoPuedeEditarEquipo() throws Exception {
        logearComo(5L, false);

        this.mockMvc.perform(get("/equipos/5/editar"))
                .andExpect(status().isUnauthorized());
        this.mockMvc.perform(post("/equipos/5/editar").param("nombre", "X"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void sinLoginNoPuedeEditarEquipo() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/equipos/5/editar"))
                .andExpect(status().isUnauthorized());
        this.mockMvc.perform(post("/equipos/5/editar").param("nombre", "X"))
                .andExpect(status().isUnauthorized());
    }
}