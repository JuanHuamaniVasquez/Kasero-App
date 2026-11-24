package com.mercado.altiplano.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

/** Convierte enlaces directos a las rutas hash usadas por Angular, sin interceptar API ni archivos. */
@Controller
public class SpaController {
    @GetMapping({"/mercado", "/buscar", "/acceso", "/precios", "/comparar", "/pedidos",
            "/chats", "/mapa", "/favoritos", "/mi-puesto", "/admin", "/puesto/{id}",
            "/cotizar/{id}", "/pedido/{id}", "/chats/{id}", "/chat/{id}"})
    public String angular(HttpServletRequest request) {
        String query = request.getQueryString();
        return "redirect:/#" + request.getRequestURI() + (query == null ? "" : "?" + query);
    }
}
