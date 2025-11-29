package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.model.Pedido;
import com.pasteleria.cordova.model.Usuario;
import com.pasteleria.cordova.service.FacturaService;
import com.pasteleria.cordova.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
@RequestMapping("/factura")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private PedidoService pedidoService;

    /**
     * Descarga la factura PDF para un pedido específico
     */
    @GetMapping("/descargar/{pedidoId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> descargarFactura(@PathVariable Integer pedidoId) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = authentication.getName();
            
            // Verificar que el pedido pertenece al usuario autenticado
            Optional<Pedido> pedidoOpt = pedidoService.getPedidoById(pedidoId);
            if (!pedidoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Pedido pedido = pedidoOpt.get();
            
            // Verificar propiedad del pedido (solo el cliente dueño puede descargar)
            if (!pedido.getCliente().getUsuario().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verificar que se puede generar la factura
            if (!facturaService.puedeGenerarFactura(pedidoId)) {
                return ResponseEntity.badRequest()
                    .body(("No se puede generar factura para este pedido. Estado actual: " + 
                           pedido.getEstado()).getBytes());
            }

            // Generar PDF
            byte[] pdfBytes = facturaService.generarFacturaPDF(pedidoId);
            String nombreArchivo = facturaService.generarNombreArchivo(pedidoId);

            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error generando factura: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Previsualizar la factura PDF en el navegador
     */
    @GetMapping("/vista/{pedidoId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> previsualizarFactura(@PathVariable Integer pedidoId) {
        try {
            // Obtener el usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = authentication.getName();
            
            // Verificar que el pedido pertenece al usuario autenticado
            Optional<Pedido> pedidoOpt = pedidoService.getPedidoById(pedidoId);
            if (!pedidoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Pedido pedido = pedidoOpt.get();
            
            // Verificar propiedad del pedido
            if (!pedido.getCliente().getUsuario().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verificar que se puede generar la factura
            if (!facturaService.puedeGenerarFactura(pedidoId)) {
                return ResponseEntity.badRequest()
                    .body(("No se puede generar factura para este pedido. Estado actual: " + 
                           pedido.getEstado()).getBytes());
            }

            // Generar PDF
            byte[] pdfBytes = facturaService.generarFacturaPDF(pedidoId);

            // Configurar headers para vista en navegador
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=Factura_" + pedidoId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error generando factura: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Endpoint para administradores - descargar cualquier factura
     */
    @GetMapping("/admin/descargar/{pedidoId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> descargarFacturaAdmin(@PathVariable Integer pedidoId) {
        try {
            // Verificar que el usuario tiene rol de admin
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Verificar rol de administrador
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verificar que el pedido existe
            Optional<Pedido> pedidoOpt = pedidoService.getPedidoById(pedidoId);
            if (!pedidoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // Verificar que se puede generar la factura
            if (!facturaService.puedeGenerarFactura(pedidoId)) {
                Pedido pedido = pedidoOpt.get();
                return ResponseEntity.badRequest()
                    .body(("No se puede generar factura para este pedido. Estado actual: " + 
                           pedido.getEstado()).getBytes());
            }

            // Generar PDF
            byte[] pdfBytes = facturaService.generarFacturaPDF(pedidoId);
            String nombreArchivo = facturaService.generarNombreArchivo(pedidoId);

            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error generando factura: " + e.getMessage()).getBytes());
        }
    }
}