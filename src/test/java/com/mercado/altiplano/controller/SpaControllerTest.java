package com.mercado.altiplano.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SpaControllerTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new SpaController()).build();

    @Test
    void losEnlacesDirectosDePantallasSirvenAngular() throws Exception {
        for (String path : new String[]{"/mercado", "/buscar", "/acceso", "/precios", "/comparar",
                "/pedidos", "/chats", "/mapa", "/favoritos", "/mi-puesto", "/admin"})
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void losEnlacesConIdentificadoresTambienSirvenAngular() throws Exception {
        for (String path : new String[]{"/puesto/25", "/cotizar/25", "/pedido/25", "/chats/1", "/chat/1"})
            mvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void noInterceptaApiNiArchivosEstaticos() throws Exception {
        for (String path : new String[]{"/api/sectores", "/api/admin/puestos", "/main.js", "/assets/logo.svg"})
            mvc.perform(get(path)).andExpect(status().isNotFound());
    }
}
