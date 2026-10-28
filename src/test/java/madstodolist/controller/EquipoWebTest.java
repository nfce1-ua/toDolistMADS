package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}