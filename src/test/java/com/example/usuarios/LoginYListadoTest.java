package com.example.usuarios;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class LoginYListadoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ResultActions registrar(String email, String password) throws Exception {
        return mvc.perform(post("/api/registro").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"email\":\"" + email + "\",\"password\":\"" + password
                        + "\",\"phones\":[]}"));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private String tokenDe(ResultActions resultado) throws Exception {
        String json = resultado.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    @Test
    void correoConFormatoDelEnunciadoSeRegistra() throws Exception {
        registrar("aaaaaaa@dominio.cl", "Secreto1").andExpect(status().isCreated());
    }

    @Test
    void loginExitosoDevuelveTokenYSoloLosCamposBasicos() throws Exception {
        registrar("login.ok@mail.com", "Secreto1").andExpect(status().isCreated());

        login("login.ok@mail.com", "Secreto1")
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("login.ok@mail.com"))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.created").doesNotExist())
                .andExpect(jsonPath("$.modified").doesNotExist())
                .andExpect(jsonPath("$.last_login").doesNotExist())
                .andExpect(jsonPath("$.isactive").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        registrar("login.mal@mail.com", "Secreto1").andExpect(status().isCreated());

        login("login.mal@mail.com", "otraClave")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    void loginConCorreoInexistenteDevuelve401() throws Exception {
        login("no.existe@mail.com", "Secreto1")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    void listarSinTokenDevuelve401ComoJson() throws Exception {
        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mensaje").value("Falta el token en el header Authorization"));
    }

    @Test
    void listarConTokenInvalidoDevuelve401() throws Exception {
        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer token.falso.123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token inválido o vencido"));
    }

    @Test
    void listarConTokenValidoDevuelveUsuariosSinTokens() throws Exception {
        String token = tokenDe(registrar("listar@mail.com", "Secreto1").andExpect(status().isCreated()));

        mvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email", notNullValue()))
                .andExpect(jsonPath("$[0].token").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }
}
