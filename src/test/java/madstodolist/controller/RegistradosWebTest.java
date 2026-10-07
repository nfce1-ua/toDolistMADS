package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistradosWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    // Simula un administrador logeado con id 99
    private void logearComoAdministrador() {
        UsuarioData admin = new UsuarioData();
        admin.setId(99L);
        admin.setAdministrador(true);
        when(managerUserSession.usuarioLogeado()).thenReturn(99L);
        when(usuarioService.findById(99L)).thenReturn(admin);
    }

    @Test
    public void listadoUsuariosMuestraIdYEmail() throws Exception {
        logearComoAdministrador();
        UsuarioData ana = new UsuarioData();
        ana.setId(1L);
        ana.setEmail("ana.garcia@gmail.com");
        UsuarioData luis = new UsuarioData();
        luis.setId(2L);
        luis.setEmail("luis@ua");
        when(usuarioService.allUsuarios()).thenReturn(Arrays.asList(ana, luis));

        this.mockMvc.perform(get("/registrados"))
                .andExpect(content().string(allOf(
                        containsString("ana.garcia@gmail.com"),
                        containsString("luis@ua"))));
    }

    @Test
    public void descripcionUsuarioMuestraDatosSinPassword() throws Exception {
        logearComoAdministrador();
        UsuarioData ana = new UsuarioData();
        ana.setId(1L);
        ana.setEmail("ana.garcia@gmail.com");
        ana.setNombre("Ana García");
        ana.setPassword("secreta123");
        when(usuarioService.findById(1L)).thenReturn(ana);

        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(content().string(allOf(
                        containsString("ana.garcia@gmail.com"),
                        containsString("Ana García"),
                        not(containsString("secreta123")))));
    }

    @Test
    public void descripcionUsuarioInexistenteDevuelve404() throws Exception {
        logearComoAdministrador();

        this.mockMvc.perform(get("/registrados/50"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void sinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void usuarioNormalDevuelveNoAutorizado() throws Exception {
        UsuarioData normal = new UsuarioData();
        normal.setId(5L);
        normal.setAdministrador(false);
        when(managerUserSession.usuarioLogeado()).thenReturn(5L);
        when(usuarioService.findById(5L)).thenReturn(normal);

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized());
        this.mockMvc.perform(get("/registrados/5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void administradorPuedeCambiarBloqueo() throws Exception {
        logearComoAdministrador();

        this.mockMvc.perform(post("/registrados/1/bloqueo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));

        verify(usuarioService).cambiarBloqueo(1L);
    }

    @Test
    public void sinLoginNoPuedeCambiarBloqueo() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(post("/registrados/1/bloqueo"))
                .andExpect(status().isUnauthorized());
    }
}