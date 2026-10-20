package com.example.usuarios.dto;

/** Formato unico de mensajes de error: {"mensaje": "..."} */
public class MensajeResponse {

    private final String mensaje;

    public MensajeResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}
