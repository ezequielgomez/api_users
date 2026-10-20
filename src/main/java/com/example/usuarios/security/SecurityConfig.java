package com.example.usuarios.security;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.usuarios.dto.MensajeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    /**
     * Inyección de dependencias: un único PasswordEncoder (BCrypt) compartido como bean de Spring.
     * Quien lo usa depende de la interfaz PasswordEncoder, no de BCrypt (Strategy: el algoritmo
     * de hash se puede cambiar acá sin tocar el servicio).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Chain of Responsibility: Spring Security procesa cada request con una cadena de filtros;
     * JwtAuthFilter se agrega antes del filtro de usuario/contraseña.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   ObjectMapper objectMapper) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // La consola de H2 se muestra dentro de <iframe> de su mismo origen
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                // Matchers explícitos: con la consola de H2 hay dos servlets registrados y
                // requestMatchers("...") con String ya no sabe a cuál aplica (lanza error al arrancar)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers(antMatcher("/api/usuarios/**")).authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint(objectMapper)))
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** 401 en JSON con el mismo formato {"mensaje": "..."} que el resto de los errores. */
    private AuthenticationEntryPoint entryPoint(ObjectMapper objectMapper) {
        return (request, response, ex) -> {
            String mensaje = request.getAttribute(JwtAuthFilter.ATTR_TOKEN_INVALIDO) != null
                    ? "Token inválido o vencido"
                    : "Falta el token en el header Authorization";
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(response.getWriter(), new MensajeResponse(mensaje));
        };
    }
}
