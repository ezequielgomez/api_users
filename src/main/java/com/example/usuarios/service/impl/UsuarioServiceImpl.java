package com.example.usuarios.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.usuarios.dto.LoginRequest;
import com.example.usuarios.dto.LoginResponse;
import com.example.usuarios.dto.RegistroRequest;
import com.example.usuarios.dto.UsuarioListadoResponse;
import com.example.usuarios.dto.UsuarioResponse;
import com.example.usuarios.exception.ApiException;
import com.example.usuarios.mapper.UsuarioMapper;
import com.example.usuarios.model.Usuario;
import com.example.usuarios.repository.UsuarioRepository;
import com.example.usuarios.security.JwtService;
import com.example.usuarios.service.UsuarioService;
import com.example.usuarios.validation.ValidadorRegistro;

/**
 * Patrón Service Layer / Facade: el controller solo conoce la interfaz UsuarioService,
 * y esta clase coordina validadores, mapper, repositorio y JWT detrás de ella.
 *
 * Inyección de dependencias por constructor: todas las colaboraciones llegan como interfaces
 * o beans de Spring, nada se crea con "new" acá, lo que facilita reemplazarlas en tests.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper mapper;
    private final List<ValidadorRegistro> validadores;

    public UsuarioServiceImpl(UsuarioRepository repository,
                              JwtService jwtService,
                              PasswordEncoder passwordEncoder,
                              UsuarioMapper mapper,
                              List<ValidadorRegistro> validadores) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.validadores = validadores; // Spring los entrega ordenados según @Order
    }

    @Override
    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        // El correo se normaliza antes de validar para que todas las reglas vean el mismo valor
        request.setEmail(request.getEmail().trim().toLowerCase());

        // Chain of Responsibility: corre cada regla en orden; la primera que falla lanza ApiException
        validadores.forEach(v -> v.validar(request));

        LocalDateTime ahora = LocalDateTime.now();
        UUID id = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setName(request.getName().trim());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setCreated(ahora);
        usuario.setModified(ahora);
        usuario.setLastLogin(ahora); // usuario nuevo: coincide con created
        usuario.setToken(jwtService.generarToken(id, request.getEmail()));
        usuario.setActive(true);
        usuario.getPhones().addAll(mapper.toTelefonos(request.getPhones()));

        return mapper.toResponse(repository.save(usuario));
    }

    /**
     * Mismo mensaje para correo inexistente o contraseña incorrecta, para no revelar qué correos existen.
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = repository.findByEmailIgnoreCase(request.getEmail().trim())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!usuario.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "El usuario está inactivo");
        }

        usuario.setLastLogin(LocalDateTime.now());
        usuario.setToken(jwtService.generarToken(usuario.getId(), usuario.getEmail()));

        return mapper.toLoginResponse(repository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioListadoResponse> listar() {
        return repository.findAll().stream()
                .map(mapper::toListadoResponse)
                .toList();
    }
}
