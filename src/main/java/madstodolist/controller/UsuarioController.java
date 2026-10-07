package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.UsuarioNotFoundException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    // Solo continúa si hay un usuario logeado y es administrador
    private void comprobarAdministrador() {
        Long idLogeado = managerUserSession.usuarioLogeado();
        if (idLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }
        UsuarioData usuario = usuarioService.findById(idLogeado);
        if (usuario == null || !usuario.isAdministrador()) {
            throw new UsuarioNoAutorizadoException();
        }
    }

    @GetMapping("/registrados")
    public String listadoUsuarios(Model model) {
        comprobarAdministrador();
        model.addAttribute("usuarios", usuarioService.allUsuarios());
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable(value = "id") Long idUsuario, Model model) {
        comprobarAdministrador();
        UsuarioData usuario = usuarioService.findById(idUsuario);
        if (usuario == null) {
            throw new UsuarioNotFoundException();
        }
        model.addAttribute("usuario", usuario);
        return "descripcionUsuario";
    }

    @PostMapping("/registrados/{id}/bloqueo")
    public String cambiarBloqueo(@PathVariable(value = "id") Long idUsuario) {
        comprobarAdministrador();
        usuarioService.cambiarBloqueo(idUsuario);
        return "redirect:/registrados";
    }
}