package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.dto.CheckoutRequestDTO; // Importar el DTO
import com.pasteleria.cordova.model.Pedido;
import com.pasteleria.cordova.model.Usuario;
import com.pasteleria.cordova.service.CustomUserDetailsService;
import com.pasteleria.cordova.service.PedidoService;
import com.pasteleria.cordova.service.ResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal; // Importar BigDecimal
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private ResenaService resenaService;

    // API para procesar el pago y crear el pedido
    @PostMapping("/api/checkout")
    @ResponseBody
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO checkoutRequest, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

        // Validaciones básicas de los datos recibidos
        if (checkoutRequest.getDireccionEnvio() == null || checkoutRequest.getDireccionEnvio().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("La dirección de envío es obligatoria.");
        }
        // Considera si el costo de envío puede ser 0. Si no, ajusta la validación.
        if (checkoutRequest.getCostoEnvio() == null || checkoutRequest.getCostoEnvio().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("El costo de envío no es válido.");
        }
        if (checkoutRequest.getMetodoPago() == null || checkoutRequest.getMetodoPago().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El método de pago es obligatorio.");
        }


        try {
            // Llama al servicio con los nuevos parámetros
            Pedido pedido = pedidoService.crearPedidoDesdeCarrito(
                    usuario,
                    checkoutRequest.getMetodoPago(),
                    checkoutRequest.getDireccionEnvio(),
                    checkoutRequest.getCostoEnvio()
            );
            return ResponseEntity.ok(pedido.getId()); // Retorna el ID del pedido creado
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Vista de "Mis Compras"
    @GetMapping("/mis-compras")
    public String misCompras(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para ver tus compras.");
            return "redirect:/login";
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

        List<Pedido> pedidos = pedidoService.getPedidosByClienteWithDetalles(usuario);
        
        // Crear un mapa para almacenar qué productos ya han sido reseñados por el cliente
        Map<Integer, Boolean> productosResendados = new HashMap<>();
        
        // Para cada pedido, verificar qué productos ya tienen reseña
        for (Pedido pedido : pedidos) {
            for (com.pasteleria.cordova.model.DetallePedido detalle : pedido.getDetalles()) {
                Integer productoId = detalle.getProducto().getId();
                if (!productosResendados.containsKey(productoId)) {
                    boolean yaReseno = resenaService.clienteYaReseno(usuario.getId(), productoId);
                    productosResendados.put(productoId, yaReseno);
                }
            }
        }
        
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("productosResendados", productosResendados);
        return "cliente/mis-pedidos"; // Plantilla en directorio cliente
    }

    // Vista de detalles de un pedido específico
    @GetMapping("/{id}")
    public String verDetallePedido(@PathVariable Integer id, Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para ver los detalles del pedido.");
            return "redirect:/login";
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuarioAutenticado = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

        Pedido pedido = pedidoService.getPedidoById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!pedido.getCliente().getClienteId().equals(usuarioAutenticado.getId())) { // Asumo que Cliente tiene un ID
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para ver este pedido.");
            return "redirect:/pedidos/mis-compras";
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("detallesPedido", pedido.getDetalles()); // Los detalles se cargarán lazy si están configurados así
        return "detalle_pedido"; // Opcional: una plantilla para ver un pedido en detalle
    }

    // API para actualizar el estado de un pedido (ej: por un administrador)
    @PutMapping("/api/actualizar-estado/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarEstado(@PathVariable Integer id, @RequestParam String estado, Authentication authentication) {
        // Aquí podrías agregar lógica para verificar si el usuario tiene rol de ADMINISTRADOR
        // Ejemplo: if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) { ... }
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Pedido pedidoActualizado = pedidoService.actualizarEstadoPedido(id, estado);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}