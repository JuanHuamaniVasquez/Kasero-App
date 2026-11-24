package com.mercado.altiplano.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Sirve Angular para sus enlaces directos sin interceptar API ni archivos estáticos. */
@Controller
public class SpaController {
    @GetMapping({"/mercado", "/buscar", "/acceso", "/precios", "/comparar", "/pedidos",
            "/chats", "/mapa", "/favoritos", "/mi-puesto", "/admin", "/puesto/{id}",
            "/cotizar/{id}", "/pedido/{id}", "/chats/{id}", "/chat/{id}"})
    public String angular() {
        return "forward:/index.html";
    }
}
