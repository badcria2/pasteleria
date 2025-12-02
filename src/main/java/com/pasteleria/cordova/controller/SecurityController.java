package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 🛡️ Controlador de Seguridad para manejar intentos de ataque
 * y validar entradas sospechosas
 */
@Controller
@RequestMapping("/security")
public class SecurityController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    /**
     * 🔍 Endpoint para validar inputs antes de procesamiento
     */
    @PostMapping("/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateInput(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        String clientIp = getClientIpAddress(request);

        try {
            // 🛡️ VALIDACIÓN DE SEGURIDAD INTENSIVA
            boolean isUsernameSecure = username == null || SecurityUtils.isInputSecure(username);
            boolean isPasswordSecure = password == null || SecurityUtils.isInputSecure(password);

            if (!isUsernameSecure || !isPasswordSecure) {
                logger.warn("[SECURITY ALERT] Intento de ataque detectado desde IP: {} - Username: {}", 
                           clientIp, 
                           SecurityUtils.sanitizeInput(username));

                // 🚨 Respuesta para intentos maliciosos
                response.put("valid", false);
                response.put("message", "Input no válido detectado");
                response.put("securityAlert", true);
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // ✅ Input válido
            response.put("valid", true);
            response.put("message", "Input válido");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("[SECURITY ERROR] Error durante validación desde IP: {} - Error: {}", 
                        clientIp, e.getMessage());
            
            response.put("valid", false);
            response.put("message", "Error interno de validación");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 🔍 Endpoint para reportar intentos de ataque
     */
    @PostMapping("/report-attack")
    @ResponseBody
    public ResponseEntity<Map<String, String>> reportAttack(
            @RequestParam String attackType,
            @RequestParam(required = false) String payload,
            HttpServletRequest request) {

        String clientIp = getClientIpAddress(request);
        
        logger.warn("[SECURITY REPORT] Ataque reportado - Tipo: {} - IP: {} - Payload: {}", 
                   SecurityUtils.sanitizeInput(attackType),
                   clientIp,
                   SecurityUtils.sanitizeInput(payload));

        Map<String, String> response = new HashMap<>();
        response.put("status", "reported");
        response.put("message", "Incidente de seguridad reportado");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 🛡️ Interceptor para rutas sensibles
     */
    @RequestMapping(value = {"/admin/**", "/api/**"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> interceptSensitiveRoutes(
            HttpServletRequest request,
            @RequestParam Map<String, String> allParams) {

        String clientIp = getClientIpAddress(request);
        
        // Validar todos los parámetros
        for (Map.Entry<String, String> param : allParams.entrySet()) {
            if (!SecurityUtils.isInputSecure(param.getKey()) || 
                !SecurityUtils.isInputSecure(param.getValue())) {
                
                logger.warn("[SECURITY INTERCEPT] Intento de acceso malicioso a ruta sensible - IP: {} - Ruta: {} - Parámetro: {}={}", 
                           clientIp,
                           request.getRequestURI(),
                           SecurityUtils.sanitizeInput(param.getKey()),
                           SecurityUtils.sanitizeInput(param.getValue()));

                Map<String, String> response = new HashMap<>();
                response.put("error", "Acceso denegado");
                response.put("reason", "Parámetros maliciosos detectados");
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        }

        // Si llegamos aquí, continuar normalmente
        return null; // Permite que Spring continue con el procesamiento normal
    }

    /**
     * 🔍 Obtener IP real del cliente (considerando proxies)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}