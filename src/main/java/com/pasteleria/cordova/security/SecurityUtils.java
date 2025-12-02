package com.pasteleria.cordova.security;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 🔐 UTILIDADES DE SEGURIDAD
 * 
 * Clase utilitaria que proporciona métodos para:
 * - Sanitización de inputs contra XSS
 * - Validación de SQL injection
 * - Validación de Path Traversal
 * - Limpieza de datos de entrada
 */
@Component
public class SecurityUtils {

    // Policy factory para sanitización HTML
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("b", "i", "em", "strong")
            .allowAttributes("href").onElements("a")
            .requireRelNofollowOnLinks()
            .toFactory();

    // Patrones de seguridad mejorados
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "('|(\\-\\-)|(;)|(\\|)|(\\*)|(%)|" +
        "(union|select|insert|update|delete|drop|create|alter|exec|execute)" +
        "|(or\\s+1\\s*=\\s*1)|" +
        "(\\bor\\b.*\\bor\\b)|" +
        "(\\bwhere\\b.*\\bor\\b)|" +
        "(\\bunion\\b.*\\bselect\\b))", 
        Pattern.CASE_INSENSITIVE);
        
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<[^>]*>|javascript:|vbscript:|onload|onerror|onclick|onmouseover|" +
        "alert\\s*\\(|document\\.|window\\.|eval\\(|setTimeout\\(|" +
        "<script|</script>|<iframe|<object|<embed|<link|<style|" +
        "expression\\s*\\(|url\\s*\\(|@import|" +
        "';\\s*alert\\s*\\(", 
        Pattern.CASE_INSENSITIVE);
        
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "(\\.\\./|\\.\\.\\\\|%2e%2e%2f|%2e%2e%5c|\\.\\.%2f|\\.\\.%5c|" +
        "\\.\\.\\/|\\.\\.\\\\\\/|\\\\\\.\\.\\\\|" +
        "\\x2e\\x2e\\/|\\x2e\\x2e\\x5c|" +
        "\\.{2,}\\/|\\.{2,}\\\\)", 
        Pattern.CASE_INSENSITIVE);
        
    // Pattern para caracteres especiales maliciosos
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile(
        "(\\x00|%00|\\r\\n|\\n\\r|" +
        "\\$\\{jndi:|\\$\\{.*\\}|" +
        "\\{\\{.*\\}\\}|" +
        "%0a|%0d|%09|%20|" +
        "\\\\x[0-9a-f]{2})", 
        Pattern.CASE_INSENSITIVE);

    /**
     * 🛡️ Sanitiza input contra XSS
     * @param input String a sanitizar
     * @return String sanitizado
     */
    public static String sanitizeXSS(String input) {
        if (input == null) return null;
        
        // Usar OWASP HTML Sanitizer
        String sanitized = POLICY.sanitize(input);
        
        // Limpieza adicional de caracteres peligrosos
        sanitized = sanitized.replaceAll("<", "&lt;")
                           .replaceAll(">", "&gt;")
                           .replaceAll("\"", "&quot;")
                           .replaceAll("'", "&#x27;")
                           .replaceAll("/", "&#x2F;");
                           
        return sanitized;
    }

    /**
     * 🛡️ Valida si el input contiene patrones de SQL Injection
     * @param input String a validar
     * @return true si es seguro, false si contiene patrones maliciosos
     */
    public static boolean isSQLSafe(String input) {
        if (input == null) return true;
        
        Matcher matcher = SQL_INJECTION_PATTERN.matcher(input);
        return !matcher.find();
    }

    /**
     * 🛡️ Valida si el input contiene patrones XSS
     * @param input String a validar
     * @return true si es seguro, false si contiene XSS
     */
    public static boolean isXSSSafe(String input) {
        if (input == null) return true;
        
        Matcher matcher = XSS_PATTERN.matcher(input);
        return !matcher.find();
    }

    /**
     * 🛡️ Valida si el path contiene path traversal
     * @param path Path a validar
     * @return true si es seguro, false si contiene path traversal
     */
    public static boolean isPathSafe(String path) {
        if (path == null) return true;
        
        Matcher matcher = PATH_TRAVERSAL_PATTERN.matcher(path);
        return !matcher.find();
    }

    /**
     * 🛡️ Validación completa de input
     * @param input String a validar
     * @return true si pasa todas las validaciones de seguridad
     */
    public static boolean isInputSecure(String input) {
        if (input == null) return true;
        
        // Primero verificar caracteres maliciosos (esto debe ejecutarse ANTES que el regex permisivo)
        if (hasSpecialMaliciousChars(input)) {
            return false;
        }
        
        // Permitir algunos caracteres especiales comunes en texto normal (incluyendo : y %)
        if (input.matches("^[\\w\\s\\-.,()$%:]+$")) {
            return true; // Texto normal con caracteres permitidos
        }
        
        // Para casos que no coinciden con el patrón simple, usar validaciones específicas
        return isSQLSafe(input) && 
               isXSSSafe(input) && 
               isPathSafe(input);
    }
    
    /**
     * 🛡️ Verifica caracteres especialmente maliciosos
     * @param input String a validar
     * @return true si contiene caracteres maliciosos
     */
    private static boolean hasSpecialMaliciousChars(String input) {
        if (input == null || input.isEmpty()) return false;
        
        // Solo detectar los caracteres realmente peligrosos
        return input.contains("\\x00") || 
               input.contains("%00") ||
               input.contains("${jndi:") ||
               input.contains("{{") ||
               input.contains("\0") ||
               input.contains("\r") ||  // Carriage return
               input.contains("\n") ||  // Line feed
               input.indexOf(0) != -1; // null byte check
    }

    /**
     * 🛡️ Sanitización completa de input
     * @param input String a sanitizar
     * @return String completamente sanitizado
     */
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        
        // Aplicar todas las sanitizaciones
        String sanitized = sanitizeXSS(input);
        
        // Remover comandos SQL peligrosos
        sanitized = sanitized.replaceAll("(?i)(drop\\s+table|delete\\s+from|insert\\s+into|update\\s+.*set)", "");
        sanitized = sanitized.replaceAll("(?i)(union\\s+select|or\\s+1\\s*=\\s*1)", "");
        
        // Remover path traversal
        sanitized = sanitized.replaceAll("(\\.\\./|\\.\\.\\\\/|%2e%2e%2f)", "");
        
        // Remover caracteres de control
        sanitized = sanitized.replaceAll("[\\x00-\\x1F\\x7F]", "");
        
        // Limitar longitud
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000);
        }
        
        return sanitized.trim();
    }

    /**
     * 🛡️ Valida ID numérico
     * @param id ID a validar
     * @return true si es un ID válido y seguro
     */
    public static boolean isValidId(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        
        try {
            long numericId = Long.parseLong(id);
            return numericId > 0 && numericId <= Long.MAX_VALUE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 🛡️ Genera mensaje de error seguro
     * @param originalMessage Mensaje original
     * @return Mensaje sanitizado para mostrar al usuario
     */
    public static String createSecureErrorMessage(String originalMessage) {
        // No exponer detalles técnicos en mensajes de error
        if (originalMessage == null) {
            return "Error interno del sistema";
        }
        
        // Para seguridad, siempre retornar mensaje genérico
        return "Error interno del sistema";
    }
}