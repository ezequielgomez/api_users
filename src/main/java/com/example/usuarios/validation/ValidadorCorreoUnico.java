package com.example.usuarios.validation;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.usuarios.dto.RegistroRequest;
import com.example.usuarios.exception.ApiException;
import com.example.usuarios.repository.UsuarioRepository;

/**
 * Strategy: el correo no puede estar registrado.
 * Va último en la cadena porque es la única regla que consulta la base de datos.
 */
@Component
@Order(3)
public class ValidadorCorreoUnico implements ValidadorRegistro {

    private final UsuarioRepository repository;

    public ValidadorCorreoUnico(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validar(RegistroRequest request) {
        if (repository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya registrado");
        }
    }
}
