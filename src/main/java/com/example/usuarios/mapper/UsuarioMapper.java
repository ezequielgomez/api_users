package com.example.usuarios.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.usuarios.dto.LoginResponse;
import com.example.usuarios.dto.TelefonoDto;
import com.example.usuarios.dto.UsuarioListadoResponse;
import com.example.usuarios.dto.UsuarioResponse;
import com.example.usuarios.model.Telefono;
import com.example.usuarios.model.Usuario;

/**
 * Patrón Mapper (también llamado DTO Assembler): concentra en un solo lugar la conversión
 * entre la entidad JPA (Usuario, Telefono) y los DTOs que entran y salen de la API.
 * Así el servicio solo tiene lógica de negocio y la entidad nunca se expone directamente
 * (por ejemplo, la contraseña hasheada nunca llega a una respuesta).
 */
@Component
public class UsuarioMapper {

    /** Respuesta del registro: todos los campos, incluido el token. */
    public UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setName(u.getName());
        r.setEmail(u.getEmail());
        r.setCreated(u.getCreated());
        r.setModified(u.getModified());
        r.setLastLogin(u.getLastLogin());
        r.setToken(u.getToken());
        r.setActive(u.isActive());
        r.setPhones(toTelefonosDto(u.getPhones()));
        return r;
    }

    /** Respuesta del listado: sin el token. */
    public UsuarioListadoResponse toListadoResponse(Usuario u) {
        UsuarioListadoResponse r = new UsuarioListadoResponse();
        r.setId(u.getId());
        r.setName(u.getName());
        r.setEmail(u.getEmail());
        r.setCreated(u.getCreated());
        r.setModified(u.getModified());
        r.setLastLogin(u.getLastLogin());
        r.setActive(u.isActive());
        r.setPhones(toTelefonosDto(u.getPhones()));
        return r;
    }

    /** Respuesta del login: solo datos básicos y el token nuevo. */
    public LoginResponse toLoginResponse(Usuario u) {
        LoginResponse r = new LoginResponse();
        r.setName(u.getName());
        r.setEmail(u.getEmail());
        r.setPhones(toTelefonosDto(u.getPhones()));
        r.setToken(u.getToken());
        return r;
    }

    /** DTOs de teléfono del request a entidades. Acepta null (registro sin teléfonos). */
    public List<Telefono> toTelefonos(List<TelefonoDto> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(t -> new Telefono(t.getNumber(), t.getCitycode(), t.getContrycode()))
                .toList();
    }

    private List<TelefonoDto> toTelefonosDto(List<Telefono> telefonos) {
        return telefonos.stream()
                .map(t -> new TelefonoDto(t.getNumber(), t.getCitycode(), t.getContrycode()))
                .toList();
    }
}
