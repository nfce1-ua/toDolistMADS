package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class UsuarioGestionServiceTest {

    @Autowired
    UsuarioService usuarioService;

    private UsuarioData registrar(String email) {
        UsuarioData u = new UsuarioData();
        u.setEmail(email);
        u.setPassword("123");
        return usuarioService.registrar(u);
    }

    @Test
    public void servicioListadoUsuarios() {
        // GIVEN: dos usuarios en la BD
        registrar("ana@ua");
        registrar("luis@ua");

        // WHEN
        List<UsuarioData> usuarios = usuarioService.allUsuarios();

        // THEN
        assertThat(usuarios).hasSize(2);
        assertThat(usuarios).extracting(UsuarioData::getEmail)
                .containsExactly("ana@ua", "luis@ua");
    }

    @Test
    public void soloPuedeHaberUnAdministrador() {
        // GIVEN: un administrador registrado
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@ua");
        admin.setPassword("123");
        admin.setAdministrador(true);
        usuarioService.registrar(admin);
        assertThat(usuarioService.existeAdministrador()).isTrue();

        // WHEN, THEN: registrar otro administrador lanza excepción
        UsuarioData otro = new UsuarioData();
        otro.setEmail("otro@ua");
        otro.setPassword("456");
        otro.setAdministrador(true);
        Assertions.assertThrows(UsuarioServiceException.class,
                () -> usuarioService.registrar(otro));
    }
}