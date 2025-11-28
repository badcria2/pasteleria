package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.service.CustomUserDetailsService;
import com.pasteleria.cordova.service.PedidoService;
import com.pasteleria.cordova.service.ProductoService;
import com.pasteleria.cordova.service.ResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/resenas")
public class ResenaClienteController {

    @Autowired
    private ResenaService resenaService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Mostrar formulario de reseña para un producto específico de un pedido
     */
    @GetMapping("/crear")
    public String mostrarFormularioResena(
            @RequestParam("pedidoId") Integer pedidoId,
            @RequestParam("productoId") Integer productoId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Verificar autenticación
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para escribir una reseña.");
            return "redirect:/login";
        }

        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

            // Obtener pedido y producto
            Pedido pedido = pedidoService.getPedidoByIdWithDetalles(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            Producto producto = productoService.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

			// Normalizar ruta de imagen
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
			}            // Verificar que el pedido pertenece al cliente autenticado
            if (!pedido.getCliente().getClienteId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para revisar este pedido.");
                return "redirect:/pedidos/mis-compras";
            }

            // Verificar que el pedido está en estado COMPLETADO o CANCELADO
            if (!pedido.getEstado().equals("COMPLETADO") && 
                !pedido.getEstado().equals("CANCELADO")) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Solo puedes escribir reseñas para pedidos completados o cancelados.");
                return "redirect:/pedidos/mis-compras";
            }

            // Verificar que el producto está en el pedido
            boolean productoEnPedido = pedido.getDetalles().stream()
                    .anyMatch(detalle -> detalle.getProducto().getId().equals(productoId));
            
            if (!productoEnPedido) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Este producto no está en el pedido especificado.");
                return "redirect:/pedidos/mis-compras";
            }

            // Verificar que el cliente no ha escrito ya una reseña para este producto
            if (resenaService.clienteYaReseno(usuario.getId(), productoId)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Ya has escrito una reseña para este producto.");
                return "redirect:/pedidos/mis-compras";
            }

            // Preparar modelo para el formulario
            model.addAttribute("pedido", pedido);
            model.addAttribute("producto", producto);
            model.addAttribute("resena", new Resena()); // Objeto vacío para el formulario

            return "cliente/resena-form";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al cargar el formulario de reseña: " + e.getMessage());
            return "redirect:/pedidos/mis-compras";
        }
    }

    /**
     * Procesar envío de reseña
     */
    @PostMapping("/crear")
    public String crearResena(
            @RequestParam("pedidoId") Integer pedidoId,
            @RequestParam("productoId") Integer productoId,
            @RequestParam("calificacion") Integer calificacion,
            @RequestParam("comentario") String comentario,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        // Verificar autenticación
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para escribir una reseña.");
            return "redirect:/login";
        }

        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

            // Validaciones básicas
            if (calificacion < 1 || calificacion > 5) {
                redirectAttributes.addFlashAttribute("errorMessage", "La calificación debe estar entre 1 y 5 estrellas.");
                return "redirect:/resenas/crear?pedidoId=" + pedidoId + "&productoId=" + productoId;
            }

            if (comentario == null || comentario.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "El comentario es obligatorio.");
                return "redirect:/resenas/crear?pedidoId=" + pedidoId + "&productoId=" + productoId;
            }

            if (comentario.length() > 500) {
                redirectAttributes.addFlashAttribute("errorMessage", "El comentario no puede exceder 500 caracteres.");
                return "redirect:/resenas/crear?pedidoId=" + pedidoId + "&productoId=" + productoId;
            }

            // Repetir validaciones de seguridad
            Pedido pedido = pedidoService.getPedidoByIdWithDetalles(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
            Producto producto = productoService.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (!pedido.getCliente().getClienteId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para revisar este pedido.");
                return "redirect:/pedidos/mis-compras";
            }

            if (!pedido.getEstado().equals("COMPLETADO") && 
                !pedido.getEstado().equals("CANCELADO")) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Solo puedes escribir reseñas para pedidos completados o cancelados.");
                return "redirect:/pedidos/mis-compras";
            }

            if (resenaService.clienteYaReseno(usuario.getId(), productoId)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Ya has escrito una reseña para este producto.");
                return "redirect:/pedidos/mis-compras";
            }

            // Crear y guardar la reseña
            Resena resena = new Resena();
            resena.setCliente(pedido.getCliente());
            resena.setProducto(producto);
            resena.setCalificacion(calificacion);
            resena.setComentario(comentario.trim());
            resena.setFechaCreacion(LocalDateTime.now());
            resena.setAprobada(false); // Las reseñas requieren aprobación del admin

            resenaService.guardarResena(resena);

            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Gracias por tu reseña! Será revisada por nuestro equipo antes de publicarse.");
            return "redirect:/pedidos/mis-compras";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al guardar la reseña: " + e.getMessage());
            return "redirect:/resenas/crear?pedidoId=" + pedidoId + "&productoId=" + productoId;
        }
    }
}