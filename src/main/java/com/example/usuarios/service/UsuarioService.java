package com.example.usuarios.service;

import java.util.List;

import com.example.usuarios.dto.LoginRequest;
import com.example.usuarios.dto.LoginResponse;
import com.example.usuarios.dto.RegistroRequest;
import com.example.usuarios.dto.UsuarioListadoResponse;
import com.example.usuarios.dto.UsuarioResponse;

/**
 * Patrón Facade: única puerta de entrada a la lógica de usuarios para el controller.
 * Inversión de dependencias: el controller depende de esta interfaz, no de UsuarioServiceImpl.
 */
public interface UsuarioService {

    /**
     * Registra un usuario nuevo: valida correo y contraseña con las regex configuradas,
     * verifica que el correo no exista y genera el token.
     */
    UsuarioResponse registrar(RegistroRequest request);

    /**
     * Valida correo y contraseña, genera un token nuevo y actualiza last_login.
     */
    LoginResponse login(LoginRequest request);

    /**
     * Devuelve todos los usuarios registrados, sin sus tokens.
     */
    List<UsuarioListadoResponse> listar();
}
