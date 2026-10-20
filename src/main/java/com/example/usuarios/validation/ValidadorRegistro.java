package com.example.usuarios.validation;

import com.example.usuarios.dto.RegistroRequest;

/**
 * Patrón Strategy: cada regla de validación del registro es una implementación de esta interfaz.
 *
 * Patrón Chain of Responsibility: el servicio recibe todas las implementaciones como una lista
 * (ordenada con @Order) y las ejecuta una tras otra; la primera que falla corta la cadena
 * lanzando ApiException.
 *
 * Para agregar una regla nueva alcanza con crear otra clase @Component que implemente esta
 * interfaz: no hay que tocar el servicio (principio abierto/cerrado).
 */
public interface ValidadorRegistro {

    /**
     * @throws com.example.usuarios.exception.ApiException si la regla no se cumple
     */
    void validar(RegistroRequest request);
}
