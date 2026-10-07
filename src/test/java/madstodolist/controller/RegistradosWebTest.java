package madstodolist.controller;

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

    @Test
    public void listadoUsuariosMuestraIdYEmail() throws Exception {
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
        // GIVEN: un usuario con contraseña en el servicio
        UsuarioData ana = new UsuarioData();
        ana.setId(1L);
        ana.setEmail("ana.garcia@gmail.com");
        ana.setNombre("Ana García");
        ana.setPassword("secreta123");
        when(usuarioService.findById(1L)).thenReturn(ana);

        // WHEN, THEN: la descripción muestra sus datos, pero no la contraseña
        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(content().string(allOf(
                        containsString("ana.garcia@gmail.com"),
                        containsString("Ana García"),
                        not(containsString("secreta123")))));
    }

    @Test
    public void descripcionUsuarioInexistenteDevuelve404() throws Exception {
        // GIVEN: el servicio no encuentra el usuario 50 (el mock devuelve null)

        // WHEN, THEN: la petición devuelve 404
        this.mockMvc.perform(get("/registrados/50"))
                .andExpect(status().isNotFound());
    }
}