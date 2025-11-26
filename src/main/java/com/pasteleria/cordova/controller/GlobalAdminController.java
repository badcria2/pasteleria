package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackages = "com.pasteleria.cordova.controller")
public class GlobalAdminController {

    @Autowired
    private NotificacionService notificacionService;

    /**
     * Agrega automáticamente las notificaciones a todas las vistas del admin
     */
    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpServletRequest request) {
        // Solo agregar notificaciones en rutas de admin
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.startsWith("/admin")) {
            try {
                // Agregar datos de notificaciones
                model.addAttribute("notificacionesAlertas", notificacionService.obtenerNotificacionesAlertas());
                model.addAttribute("notificacionesMensajes", notificacionService.obtenerNotificacionesMensajes());
                model.addAttribute("totalNotificaciones", notificacionService.contarNotificacionesPendientes());
                model.addAttribute("totalMensajes", notificacionService.contarMensajesPendientes());
            } catch (Exception e) {
                // En caso de error, usar valores por defecto
                model.addAttribute("notificacionesAlertas", java.util.Collections.emptyList());
                model.addAttribute("notificacionesMensajes", java.util.Collections.emptyList());
                model.addAttribute("totalNotificaciones", 0);
                model.addAttribute("totalMensajes", 0);
            }
        }
    }
}