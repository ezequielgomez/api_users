package com.example.usuarios.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.usuarios.dto.MensajeResponse;

/**
 * Todos los errores se devuelven como JSON con el formato {"mensaje": "..."}.
 *
 * Manejo centralizado de errores (@RestControllerAdvice, basado en el patrón Interceptor/AOP):
 * los controllers y servicios solo lanzan excepciones y esta clase las traduce a respuestas HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<MensajeResponse> handleApi(ApiException ex) {
        return json(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MensajeResponse> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return json(HttpStatus.BAD_REQUEST, mensaje.isBlank() ? "Datos inválidos" : mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MensajeResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return json(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<MensajeResponse> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return json(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type no soportado: solo se acepta application/json");
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<MensajeResponse> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        return json(HttpStatus.NOT_ACCEPTABLE, "Solo se puede responder application/json");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<MensajeResponse> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return json(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido para este endpoint");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<MensajeResponse> handleNotFound(NoResourceFoundException ex) {
        return json(HttpStatus.NOT_FOUND, "Recurso no encontrado");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MensajeResponse> handleIntegrity(DataIntegrityViolationException ex) {
        // Por ejemplo, dos registros simultaneos con el mismo correo
        return json(HttpStatus.CONFLICT, "El correo ya registrado");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeResponse> handleGeneric(Exception ex) {
        log.error("Error no controlado", ex);
        return json(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private ResponseEntity<MensajeResponse> json(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new MensajeResponse(mensaje));
    }
}
