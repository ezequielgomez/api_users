package com.example.usuarios.dto;

import java.util.List;

/**
 * Respuesta del login: datos básicos del usuario y el token nuevo.
 */
public class LoginResponse {

    private String name;
    private String email;
    private List<TelefonoDto> phones;
    private String token;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<TelefonoDto> getPhones() {
        return phones;
    }

    public void setPhones(List<TelefonoDto> phones) {
        this.phones = phones;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
