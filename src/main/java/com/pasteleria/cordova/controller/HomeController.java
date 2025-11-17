package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.model.Producto;
import com.pasteleria.cordova.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/")
    public String home(Model model, @RequestParam(name = "search", required = false) String search) {
        List<Producto> productos;
        if (search != null && !search.isEmpty()) {
            productos = productoService.searchProductos(search);
        } else {
            productos = productoService.findAllProductos();
        }
        model.addAttribute("productos", productos);
        return "index"; // Esto buscará src/main/resources/templates/index.html
    }

    @GetMapping("/contacto")
    public String showContactPage() {
        return "contacto"; // Esto buscará src/main/resources/templates/contacto.html
    }
}