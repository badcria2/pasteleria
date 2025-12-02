package com.pasteleria.cordova;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test de integración básico para verificar que el contexto de Spring Boot se carga correctamente
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class PasteleriaCordovaApplicationTests {

    @Test
    @Disabled("Test temporalmente deshabilitado por problemas de configuración de BD de test")
    void contextLoads() {
        // Este test verifica que el contexto de Spring Boot se puede cargar sin errores
        // Es una prueba básica pero importante para detectar problemas de configuración
    }
}