package com.example.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// la autenticación es solo por JWT
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class RegistroUsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistroUsuariosApplication.class, args);
    }
}
