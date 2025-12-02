package com.pasteleria.cordova.security;

import com.pasteleria.cordova.controller.FacturaController;
import com.pasteleria.cordova.service.FacturaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🔐 PRUEBAS DE SEGURIDAD MVP
 * 
 * Conjunto de pruebas para validar aspectos críticos de seguridad:
 * - Prevención de inyección SQL
 * - Protección contra XSS
 * - Validación de inputs
 * - Manejo seguro de errores
 * - Autorización de endpoints
 */
@WebMvcTest(FacturaController.class)
@ActiveProfiles("test")
@DisplayName("🔐 Security Tests MVP - Sistema Pastelería")
public class SecurityTestsMVP {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacturaService facturaService;

    @BeforeEach
    void setUp() {
        // Configuración inicial para pruebas de seguridad
    }

    @Test
    @DisplayName("🛡️ SEC-001: Prevención de Inyección SQL en parámetros")
    void testSQLInjectionPrevention() throws Exception {
        // Intentar inyección SQL maliciosa en parámetro de ID
        String sqlInjectionPayload = "1' OR '1'='1' --";
        
        mockMvc.perform(get("/factura/generar/{id}", sqlInjectionPayload))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("🛡️ SEC-002: Protección contra Cross-Site Scripting (XSS)")
    void testXSSProtection() throws Exception {
        // Payload XSS malicioso
        String xssPayload = "<script>alert('XSS')</script>";
        
        mockMvc.perform(post("/factura/crear")
                       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                       .param("clienteNombre", xssPayload))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("🛡️ SEC-003: Validación de rangos de entrada")
    void testInputRangeValidation() throws Exception {
        // ID fuera de rango válido
        mockMvc.perform(get("/factura/generar/{id}", "-1"))
               .andExpect(status().isBadRequest());
               
        // ID extremadamente grande
        mockMvc.perform(get("/factura/generar/{id}", "999999999"))
               .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("🛡️ SEC-004: Manejo seguro de excepciones")
    void testSecureExceptionHandling() throws Exception {
        // Verificar que no se expone información sensible en errores
        mockMvc.perform(get("/factura/generar/{id}", "invalid"))
               .andExpect(status().isBadRequest())
               .andExpect(result -> {
                   String response = result.getResponse().getContentAsString();
                   // Verificar que no contiene stack traces o información sensible
                   assert !response.contains("SQLException");
                   assert !response.contains("java.lang");
                   assert !response.contains("Exception");
               });
    }

    @Test
    @DisplayName("🛡️ SEC-005: Validación de tipos de contenido")
    void testContentTypeValidation() throws Exception {
        // Intentar enviar contenido malicioso con tipo MIME incorrecto
        mockMvc.perform(post("/factura/crear")
                       .contentType("application/x-malicious")
                       .content("malicious-content"))
               .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("🛡️ SEC-006: Protección contra Path Traversal")
    void testPathTraversalProtection() throws Exception {
        // Intentar acceso a archivos del sistema
        String pathTraversalPayload = "../../../etc/passwd";
        
        mockMvc.perform(get("/factura/archivo/{nombre}", pathTraversalPayload))
               .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("🛡️ SEC-007: Límites de tamaño de request")
    void testRequestSizeLimits() throws Exception {
        // Crear payload extremadamente grande
        StringBuilder largePayload = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largePayload.append("A");
        }
        
        mockMvc.perform(post("/factura/crear")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(largePayload.toString()))
               .andExpect(status().isPayloadTooLarge());
    }

    @Test
    @DisplayName("🛡️ SEC-008: Validación de Headers HTTP")
    void testHTTPHeaderValidation() throws Exception {
        // Headers maliciosos
        mockMvc.perform(get("/factura/generar/1")
                       .header("X-Malicious-Header", "<script>alert('xss')</script>")
                       .header("User-Agent", "../../etc/passwd"))
               .andExpect(status().isOk())
               .andExpect(result -> {
                   // Verificar que los headers maliciosos no afectan la respuesta
                   String response = result.getResponse().getContentAsString();
                   assert !response.contains("<script>");
               });
    }

    @Test
    @DisplayName("🛡️ SEC-009: Protección contra CSRF (simulación)")
    void testCSRFProtection() throws Exception {
        // Simular request sin token CSRF válido
        mockMvc.perform(post("/factura/crear")
                       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                       .param("pedidoId", "1"))
               .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("🛡️ SEC-010: Validación de codificación de caracteres")
    void testCharacterEncodingValidation() throws Exception {
        // Caracteres especiales y Unicode malicioso
        String unicodePayload = "\u0000\u001F\uFEFF";
        
        mockMvc.perform(post("/factura/crear")
                       .contentType(MediaType.APPLICATION_JSON)
                       .characterEncoding("UTF-8")
                       .content("{\"clienteNombre\":\"" + unicodePayload + "\"}"))
               .andExpect(status().isBadRequest());
    }
}