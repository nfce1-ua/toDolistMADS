package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginRegistroWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void registroMuestraCheckSiNoHayAdministrador() throws Exception {
        when(usuarioService.existeAdministrador()).thenReturn(false);

        this.mockMvc.perform(get("/registro"))
                .andExpect(content().string(containsString("name=\"administrador\"")));
    }

    @Test
    public void registroOcultaCheckSiYaHayAdministrador() throws Exception {
        when(usuarioService.existeAdministrador()).thenReturn(true);

        this.mockMvc.perform(get("/registro"))
                .andExpect(content().string(not(containsString("name=\"administrador\""))));
    }

    @Test
    public void loginDeAdministradorRedirigeAlListado() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(1L);
        admin.setAdministrador(true);
        when(usuarioService.login("admin@ua", "123"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("admin@ua")).thenReturn(admin);

        this.mockMvc.perform(post("/login")
                        .param("eMail", "admin@ua")
                        .param("password", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }

    @Test
    public void loginUsuarioBloqueadoMuestraError() throws Exception {
        when(usuarioService.login("ana@ua", "123"))
                .thenReturn(UsuarioService.LoginStatus.USER_BLOCKED);

        this.mockMvc.perform(post("/login")
                        .param("eMail", "ana@ua")
                        .param("password", "123"))
                .andExpect(content().string(containsString("Usuario bloqueado")));
    }
}