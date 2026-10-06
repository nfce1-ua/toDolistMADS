package madstodolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class AcercaDeWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void getAboutDevuelveNombreAplicacion() throws Exception {
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(containsString("ToDoList")));
    }

    @Test
    public void aboutSinLoginMuestraEnlacesLoginYRegistro() throws Exception {
        // GIVEN: ningún usuario logeado
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN: aparecen login y registro, y no el menú de usuario
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(allOf(
                        containsString("Iniciar sesión"),
                        containsString("Registrarse"),
                        not(containsString("Cerrar sesión")))));
    }

    @Test
    public void aboutConLoginMuestraBarraDeUsuario() throws Exception {
        // GIVEN: un usuario logeado
        UsuarioData ana = new UsuarioData();
        ana.setId(1L);
        ana.setNombre("Ana García");
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(usuarioService.findById(1L)).thenReturn(ana);

        // WHEN, THEN: aparece el menú común con su nombre
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(allOf(
                        containsString("Tareas"),
                        containsString("Cerrar sesión Ana García"),
                        not(containsString("Registrarse")))));
    }
}