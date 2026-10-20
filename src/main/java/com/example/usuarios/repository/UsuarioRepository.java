package com.example.usuarios.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.usuarios.model.Usuario;

/**
 * Patrón Repository: abstrae el acceso a datos de Usuario. Spring Data JPA genera la
 * implementación a partir de los nombres de los métodos; el servicio no conoce SQL ni JPA.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);
}
