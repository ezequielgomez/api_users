package com.example.usuarios.validation;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.usuarios.dto.RegistroRequest;
import com.example.usuarios.exception.ApiException;

/**
 * Strategy: la contraseña debe cumplir la regex configurada en app.validation.password-regex.
 * El mensaje de error también es configurable (app.validation.password-message).
 */
@Component
@Order(2)
public class ValidadorContrasena implements ValidadorRegistro {

    private final Pattern passwordPattern;
    private final String passwordMessage;

    public ValidadorContrasena(@Value("${app.validation.password-regex}") String passwordRegex,
                               @Value("${app.validation.password-message}") String passwordMessage) {
        this.passwordPattern = Pattern.compile(passwordRegex);
        this.passwordMessage = passwordMessage;
    }

    @Override
    public void validar(RegistroRequest request) {
        if (!passwordPattern.matcher(request.getPassword()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, passwordMessage);
        }
    }
}
