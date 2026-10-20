package com.example.usuarios;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class RegistroControllerTest {

    private static final String URL = "/api/registro";

    @Autowired
    private MockMvc mvc;

    // Se lee de application.properties para que el test no dependa del texto exacto
    @Value("${app.validation.password-message}")
    private String mensajeContrasena;

    private static String body(String name, String email, String password, String phones) {
        return "{\"name\":" + (name == null ? "null" : "\"" + name + "\"")
                + ",\"email\":\"" + email + "\""
                + ",\"password\":\"" + password + "\""
                + ",\"phones\":" + phones + "}";
    }

    private static final String PHONES_OK =
            "[{\"number\":\"1234567\",\"citycode\":\"1\",\"contrycode\":\"57\"}]";

    private ResultActions registrar(String json) throws Exception {
        return mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void registroExitosoDevuelve201ConTodosLosCampos() throws Exception {
        registrar(body("Juan Rodriguez", "juan@rodriguez.org", "Hunter2", PHONES_OK))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Juan Rodriguez"))
                .andExpect(jsonPath("$.email").value("juan@rodriguez.org"))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.modified", notNullValue()))
                .andExpect(jsonPath("$.last_login", notNullValue()))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.isactive").value(true))
                .andExpect(jsonPath("$.phones", hasSize(1)))
                .andExpect(jsonPath("$.phones[0].number").value("1234567"))
                .andExpect(jsonPath("$.phones[0].citycode").value("1"))
                .andExpect(jsonPath("$.phones[0].contrycode").value("57"))
                // la contrasena nunca se devuelve
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void usuarioNuevoTieneLastLoginIgualACreated() throws Exception {
        String resp = registrar(body("Ana", "ana@mail.com", "Secreto1", "[]"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode n = new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp);
        org.junit.jupiter.api.Assertions.assertEquals(n.get("created").asText(), n.get("last_login").asText());
        org.junit.jupiter.api.Assertions.assertEquals(n.get("created").asText(), n.get("modified").asText());
    }

    @Test
    void correoDuplicadoDevuelve409() throws Exception {
        registrar(body("Luis", "luis@mail.com", "Secreto1", "[]")).andExpect(status().isCreated());
        registrar(body("Luis 2", "LUIS@mail.com", "Secreto1", "[]"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("El correo ya registrado"));
    }

    @Test
    void correoConFormatoInvalidoDevuelve400() throws Exception {
        registrar(body("Pepe", "esto-no-es-un-correo", "Secreto1", "[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El formato del correo es inválido"));
    }

    @Test
    void contrasenaInvalidaDevuelve400() throws Exception {
        registrar(body("Pepe", "pepe@mail.com", "123", "[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(mensajeContrasena));
    }

    @Test
    void contrasenaSinMayusculaDevuelve400() throws Exception {
        registrar(body("Pepe", "pepe2@mail.com", "hunter2", "[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(mensajeContrasena));
    }

    @Test
    void contrasenaSinNumeroDevuelve400() throws Exception {
        registrar(body("Pepe", "pepe3@mail.com", "Secreto", "[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(mensajeContrasena));
    }

    @Test
    void faltaElNombreDevuelve400() throws Exception {
        registrar(body(null, "sinnombre@mail.com", "Secreto1", "[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El nombre es obligatorio"));
    }

    @Test
    void telefonoIncompletoDevuelve400() throws Exception {
        registrar(body("Tel", "tel@mail.com", "Secreto1",
                "[{\"number\":\"\",\"citycode\":\"1\",\"contrycode\":\"57\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El número de teléfono es obligatorio"));
    }

    @Test
    void jsonMalFormadoDevuelve400ComoJson() throws Exception {
        registrar("{ esto no es json")
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la petición no es un JSON válido"));
    }

    @Test
    void contentTypeQueNoEsJsonDevuelve415ComoJson() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje", containsString("application/json")));
    }

    @Test
    void metodoNoPermitidoDevuelve405ComoJson() throws Exception {
        mvc.perform(get(URL))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje", not(org.hamcrest.Matchers.emptyOrNullString())));
    }

    @Test
    void rutaInexistenteDevuelve404ComoJson() throws Exception {
        mvc.perform(get("/api/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("Recurso no encontrado"));
    }
}
