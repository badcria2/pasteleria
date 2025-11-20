package com.pasteleria.cordova.controller;


import com.pasteleria.cordova.model.Pedido;
import com.pasteleria.cordova.model.Usuario;
import com.pasteleria.cordova.service.CustomUserDetailsService;
import com.pasteleria.cordova.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // API para procesar el pago y crear el pedido
    @PostMapping("/api/checkout")
    @ResponseBody
    public ResponseEntity<?> checkout(@RequestParam String metodoPago, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

        try {
            Pedido pedido = pedidoService.crearPedidoDesdeCarrito(usuario, metodoPago);
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
        model.addAttribute("pedidos", pedidos);
        return "mis_compras"; // Nueva plantilla Thymeleaf
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

        // Asegurarse de que el pedido pertenece al cliente autenticado
        if (!pedido.getCliente().getUsuario().getId().equals(usuarioAutenticado.getId())) {
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
        if (authentication == null || !authentication.isAuthenticated() /* || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) */) {
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