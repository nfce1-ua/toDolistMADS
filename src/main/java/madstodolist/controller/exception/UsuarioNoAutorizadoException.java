package madstodolist.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED,
        reason = "No tienes permiso suficiente para acceder a esta página")
public class UsuarioNoAutorizadoException extends RuntimeException {
}