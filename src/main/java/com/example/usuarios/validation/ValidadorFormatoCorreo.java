package com.example.usuarios.validation;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.usuarios.dto.RegistroRequest;
import com.example.usuarios.exception.ApiException;

/** Strategy: el correo debe cumplir la regex configurada en app.validation.email-regex. */
@Component
@Order(1)
public class ValidadorFormatoCorreo implements ValidadorRegistro {

    private final Pattern emailPattern;

    public ValidadorFormatoCorreo(@Value("${app.validation.email-regex}") String emailRegex) {
        this.emailPattern = Pattern.compile(emailRegex);
    }

    @Override
    public void validar(RegistroRequest request) {
        if (!emailPattern.matcher(request.getEmail()).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El formato del correo es inválido");
        }
    }
}
