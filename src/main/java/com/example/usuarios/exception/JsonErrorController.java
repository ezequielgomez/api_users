package com.example.usuarios.exception;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.usuarios.dto.MensajeResponse;

import io.swagger.v3.oas.annotations.Hidden;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Reemplaza al /error de Spring Boot. Atiende los errores que no llegan a GlobalExceptionHandler
 * (por ejemplo, los que ocurren en filtros o los rechazados por el firewall de Spring Security),
 * para que también salgan con el formato {"mensaje": "..."}.
 */
@Hidden // no se muestra en Swagger
@RestController
public class JsonErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<MensajeResponse> error(HttpServletRequest request) {
        Object codigo = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = codigo instanceof Integer c ? HttpStatus.resolve(c) : null;
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String mensaje = switch (status) {
            case BAD_REQUEST -> "Petición inválida";
            case UNAUTHORIZED -> "No autorizado";
            case FORBIDDEN -> "Acceso denegado";
            case NOT_FOUND -> "Recurso no encontrado";
            case METHOD_NOT_ALLOWED -> "Método HTTP no permitido para este endpoint";
            default -> status.is5xxServerError() ? "Error interno del servidor" : status.getReasonPhrase();
        };

        // Content-Type fijo: siempre JSON, sin importar el header Accept
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new MensajeResponse(mensaje));
    }
}
