package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.EquipoNotFoundException;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EquipoController {

    @Autowired
    EquipoService equipoService;

    @Autowired
    ManagerUserSession managerUserSession;

    private Long comprobarUsuarioLogeado() {
        Long idLogeado = managerUserSession.usuarioLogeado();
        if (idLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }
        return idLogeado;
    }

    // Solo continúa si hay un usuario logeado y es administrador
    private void comprobarAdministrador() {
        Long idLogeado = comprobarUsuarioLogeado();
        UsuarioData usuario = usuarioService.findById(idLogeado);
        if (usuario == null || !usuario.isAdministrador()) {
            throw new UsuarioNoAutorizadoException();
        }
    }

    @GetMapping("/equipos")
    public String listadoEquipos(Model model) {
        comprobarUsuarioLogeado();
        model.addAttribute("equipos", equipoService.findAllOrdenadoPorNombre());
        return "listaEquipos";
    }

    @GetMapping("/equipos/{id}")
    public String miembrosEquipo(@PathVariable(value = "id") Long idEquipo, Model model) {
        Long idLogeado = comprobarUsuarioLogeado();
        try {
            model.addAttribute("equipo", equipoService.recuperarEquipo(idEquipo));
            model.addAttribute("usuarios", equipoService.usuariosEquipo(idEquipo));
            model.addAttribute("pertenece", equipoService.usuarioPerteneceAEquipo(idEquipo, idLogeado));
        } catch (EquipoServiceException e) {
            throw new EquipoNotFoundException();
        }
        return "equipo";
    }

    @GetMapping("/equipos/nuevo")
    public String formNuevoEquipo() {
        comprobarUsuarioLogeado();
        return "formNuevoEquipo";
    }

    @PostMapping("/equipos/nuevo")
    public String nuevoEquipo(@RequestParam("nombre") String nombre,
                              RedirectAttributes flash, Model model) {
        comprobarUsuarioLogeado();
        try {
            equipoService.crearEquipo(nombre);
        } catch (EquipoServiceException e) {
            model.addAttribute("error", e.getMessage());
            return "formNuevoEquipo";
        }
        flash.addFlashAttribute("mensaje", "Equipo creado correctamente");
        return "redirect:/equipos";
    }
    @PostMapping("/equipos/{id}/unirse")
    public String unirse(@PathVariable(value = "id") Long idEquipo, RedirectAttributes flash) {
        Long idLogeado = comprobarUsuarioLogeado();
        try {
            equipoService.añadirUsuarioAEquipo(idEquipo, idLogeado);
            flash.addFlashAttribute("mensaje", "Te has unido al equipo");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/equipos/" + idEquipo;
    }

    @PostMapping("/equipos/{id}/salir")
    public String salir(@PathVariable(value = "id") Long idEquipo, RedirectAttributes flash) {
        Long idLogeado = comprobarUsuarioLogeado();
        try {
            equipoService.eliminarUsuarioDeEquipo(idEquipo, idLogeado);
            flash.addFlashAttribute("mensaje", "Has salido del equipo");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/equipos/" + idEquipo;
    }
    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/equipos/{id}/editar")
    public String formEditarEquipo(@PathVariable(value = "id") Long idEquipo, Model model) {
        comprobarAdministrador();
        try {
            model.addAttribute("equipo", equipoService.recuperarEquipo(idEquipo));
        } catch (EquipoServiceException e) {
            throw new EquipoNotFoundException();
        }
        return "formEditarEquipo";
    }

    @PostMapping("/equipos/{id}/editar")
    public String editarEquipo(@PathVariable(value = "id") Long idEquipo,
                               @RequestParam("nombre") String nombre,
                               RedirectAttributes flash) {
        comprobarAdministrador();
        try {
            equipoService.renombrarEquipo(idEquipo, nombre);
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/equipos/" + idEquipo + "/editar";
        }
        flash.addFlashAttribute("mensaje", "Equipo modificado correctamente");
        return "redirect:/equipos/" + idEquipo;
    }
}