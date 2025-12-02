package com.pasteleria.cordova.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTestFinal {

    @Mock
    private PedidoService pedidoService;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private FacturaService facturaService;

    @Test
    void testGenerarNombreArchivo() {
        // Given
        Integer pedidoId = 123;

        // When
        String resultado = facturaService.generarNombreArchivo(pedidoId);

        // Then
        assertEquals("Factura_Pedido_123.pdf", resultado);
    }

    @Test
    void testGenerarNombreArchivo_ConIdNulo() {
        // Given
        Integer pedidoId = null;

        // When 
        String resultado = facturaService.generarNombreArchivo(pedidoId);

        // Then - El método debería retornar un string válido
        assertNotNull(resultado);
        assertTrue(resultado.length() > 0);
    }

    @Test
    void testGenerarNombreArchivo_ConIdCero() {
        // Given
        Integer pedidoId = 0;

        // When
        String resultado = facturaService.generarNombreArchivo(pedidoId);

        // Then
        assertEquals("Factura_Pedido_0.pdf", resultado);
    }

    @Test
    void testGenerarNombreArchivo_ConIdNegativo() {
        // Given
        Integer pedidoId = -1;

        // When
        String resultado = facturaService.generarNombreArchivo(pedidoId);

        // Then
        assertEquals("Factura_Pedido_-1.pdf", resultado);
    }

    @Test
    void testGenerarNombreArchivo_ConIdGrande() {
        // Given
        Integer pedidoId = 999999;

        // When
        String resultado = facturaService.generarNombreArchivo(pedidoId);

        // Then
        assertEquals("Factura_Pedido_999999.pdf", resultado);
    }

    @Test
    void testFacturaService_NoEsNulo() {
        // Assert que el servicio fue inyectado correctamente
        assertNotNull(facturaService);
    }
}