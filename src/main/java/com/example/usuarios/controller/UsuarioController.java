package com.example.usuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.usuarios.dto.LoginRequest;
import com.example.usuarios.dto.LoginResponse;
import com.example.usuarios.dto.RegistroRequest;
import com.example.usuarios.dto.UsuarioListadoResponse;
import com.example.usuarios.dto.UsuarioResponse;
import com.example.usuarios.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * Arquitectura en capas (Controller -> Service -> Repository) / MVC: el controller solo recibe
 * el request HTTP, delega en la interfaz UsuarioService y devuelve la respuesta.
 */
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    /**
     * POST /api/registro
     * Solo acepta application/json. 201 Created con el usuario y su token.
     */
    @PostMapping(value = "/registro", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }

    /**
     * POST /api/login
     * Recibe {"email": "...", "password": "..."}. 200 OK con name, email, phones y un token nuevo,
     * 401 si el correo o la contraseña son incorrectos.
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    /**
     * GET /api/usuarios
     * Protegido por JwtAuthFilter (ver SecurityConfig): requiere Authorization: Bearer &lt;token&gt;.
     * 200 OK con la lista de usuarios (sin tokens).
     */
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioListadoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
