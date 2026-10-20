package com.example.usuarios.security;

import java.util.Optional;
import java.util.UUID;

/**
 * Inversión de dependencias: el servicio y el filtro dependen de esta interfaz, no de jjwt.
 * La implementación (JwtServiceImpl) se puede reemplazar sin tocar a quienes la usan.
 */
public interface JwtService {

    /**
     * Genera un JWT firmado con el correo como subject y el id del usuario como claim.
     */
    String generarToken(UUID userId, String email);

    /**
     * Verifica firma y vencimiento del token.
     * @return el email (subject) si el token es válido, vacío si no
     */
    Optional<String> obtenerEmail(String token);
}
