package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.model.Carrito;
import com.pasteleria.cordova.model.DetalleCarrito;
import com.pasteleria.cordova.model.Usuario;
import com.pasteleria.cordova.service.CarritoService;
import com.pasteleria.cordova.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;
    @Autowired
    private CustomUserDetailsService customUserDetailsService; // Para obtener el objeto Usuario completo

    // Muestra la vista del carrito
    @GetMapping
    public String verCarrito(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

        // Obtener el carrito del usuario para enviarlo a la vista si es necesario
        Carrito carrito = carritoService.getCarritoByCliente(usuario);
        model.addAttribute("carrito", carrito);
        model.addAttribute("detallesCarrito", carrito.getDetalles());

        return "cliente/carrito";
    }

    // API para obtener los detalles del carrito (útil para JS)
    @GetMapping("/api")
    @ResponseBody
    @Transactional
    public ResponseEntity<List<Map<String, Object>>> getCarritoApi(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

        List<DetalleCarrito> detalles = carritoService.getDetallesCarrito(usuario);
        List<Map<String, Object>> response = detalles.stream().map(detalle -> {
            Map<String, Object> item = new HashMap<>();
            item.put("productoId", detalle.getProducto().getId());
            item.put("nombre", detalle.getProducto().getNombre());
            item.put("precio", detalle.getPrecioUnitario());
            item.put("cantidad", detalle.getCantidad());
            item.put("imagen", detalle.getProducto().getImagen()); // Para mostrar imagen en el carrito
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


    // API para añadir un producto al carrito
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addProducto(@RequestParam Integer productoId, @RequestParam(defaultValue = "1") Integer cantidad, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

        try {
            carritoService.addProductoToCarrito(usuario, productoId, cantidad);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API para actualizar la cantidad de un producto en el carrito
    @PutMapping("/api/update")
    @ResponseBody
    public ResponseEntity<?> updateProducto(@RequestParam Integer productoId, @RequestParam Integer cantidad, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

        try {
            carritoService.updateProductoQuantity(usuario, productoId, cantidad);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API para eliminar un producto del carrito
    @DeleteMapping("/api/remove")
    @ResponseBody
    public ResponseEntity<?> removeProducto(@RequestParam Integer productoId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = customUserDetailsService.getUsuarioByEmail(userDetails.getUsername());

        try {
            carritoService.removeProductoFromCarrito(usuario, productoId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}