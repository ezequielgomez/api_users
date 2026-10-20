package com.example.usuarios.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lee el header "Authorization: Bearer <token>" y, si el JWT es válido,
 * deja al usuario autenticado en el SecurityContext.
 *
 * Patrón Chain of Responsibility: es un eslabón de la cadena de filtros de Spring Security;
 * hace su parte y siempre pasa el request al siguiente con chain.doFilter().
 * Patrón Template Method: OncePerRequestFilter define el algoritmo (garantiza una sola
 * ejecución por request) y esta clase solo completa el paso doFilterInternal().
 * No es @Component para que Spring Boot no lo registre también como filtro de servlet.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Atributo que usa el entry point para distinguir "sin token" de "token inválido". */
    public static final String ATTR_TOKEN_INVALIDO = "jwt.invalido";

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(PREFIJO)) {
            String token = header.substring(PREFIJO.length()).trim();
            jwtService.obtenerEmail(token).ifPresentOrElse(
                    email -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(email, null, List.of())),
                    () -> request.setAttribute(ATTR_TOKEN_INVALIDO, true));
        }

        chain.doFilter(request, response);
    }
}
