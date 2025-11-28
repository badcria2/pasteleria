package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.model.Producto;
import com.pasteleria.cordova.model.Resena;
import com.pasteleria.cordova.service.ProductoService;
import com.pasteleria.cordova.service.ResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private ResenaService resenaService;

    @Autowired
    private ProductoService productoService;

    @GetMapping("/")
    public String home(@RequestParam(required = false) String categoria, Model model) {
        List<Producto> productos;
        if (categoria != null && !categoria.isEmpty()) {
            productos = productoService.findByCategoria(categoria);
        } else {
            productos = productoService.findAllProductos();
        }

		// Normalizar rutas de imágenes
		productos.forEach(producto -> {
			if (producto.getImagen() != null) {
				String imagen = producto.getImagen();
				// Si no empieza con /, agregar la ruta completa
				if (!imagen.startsWith("/")) {
					// Si ya contiene "Imagenes/" o "imagenes/", no duplicar
					if (imagen.toLowerCase().contains("imagenes/")) {
						producto.setImagen("/uploads/productos/" + imagen);
					} else {
						producto.setImagen("/uploads/productos/imagenes/" + imagen);
					}
				}
				// Si empieza con / pero no contiene uploads, corregir
				else if (!imagen.contains("/uploads/") && imagen.toLowerCase().contains("imagenes/")) {
					producto.setImagen("/uploads/productos/" + imagen.substring(1));
				}
			}
		});        model.addAttribute("productos", productos);
        model.addAttribute("categoria", categoria);

        List<Resena> reseñas = resenaService.getResenasAprobadas();
        model.addAttribute("resenas", reseñas);


        return "index"; // Esto buscará src/main/resources/templates/index.html
    }
    /**
     * Redirige a la página de login
     */
    @GetMapping("/index")
    public String indexRedirect() {
        return "redirect:/";
    }
    @GetMapping("/contacto")
    public String showContactPage() {
        return "contacto"; // Esto buscará src/main/resources/templates/contacto.html
    }
}