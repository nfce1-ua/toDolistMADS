package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UsuarioLogeadoAdvice {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    // Se añade a todos los modelos el usuario logeado (o null)
    @ModelAttribute("usuarioLogeado")
    public UsuarioData usuarioLogeado() {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) return null;
        return usuarioService.findById(idUsuario);
    }
}