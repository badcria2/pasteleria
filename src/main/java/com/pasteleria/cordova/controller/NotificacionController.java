package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Obtiene todas las notificaciones actualizadas
     */
    @GetMapping("/alertas")
    public ResponseEntity<Map<String, Object>> obtenerAlertas() {
        Map<String, Object> response = new HashMap<>();
        response.put("notificaciones", notificacionService.obtenerNotificacionesAlertas());
        response.put("total", notificacionService.contarNotificacionesPendientes());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todos los mensajes actualizados
     */
    @GetMapping("/mensajes")
    public ResponseEntity<Map<String, Object>> obtenerMensajes() {
        Map<String, Object> response = new HashMap<>();
        response.put("mensajes", notificacionService.obtenerNotificacionesMensajes());
        response.put("total", notificacionService.contarMensajesPendientes());
        return ResponseEntity.ok(response);
    }

    /**
     * Maneja el clic en una notificación específica
     */
    @PostMapping("/marcar-leida")
    public ResponseEntity<Map<String, String>> marcarComoLeida(
            @RequestParam String tipo,
            @RequestParam Integer id) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Aquí podrías implementar lógica para marcar como leída
            // Por ahora solo retornamos la URL de redirección
            String url = "/admin/dashboard";
            
            if ("pedido".equals(tipo)) {
                url = "/admin/pedidos";
            } else if ("resena".equals(tipo)) {
                url = "/admin/resenas";
            }
            
            response.put("status", "success");
            response.put("redirectUrl", url);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al procesar la notificación");
        }
        
        return ResponseEntity.ok(response);
    }
}