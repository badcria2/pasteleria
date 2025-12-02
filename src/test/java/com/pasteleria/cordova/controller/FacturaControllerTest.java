package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.service.FacturaService;
import com.pasteleria.cordova.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaControllerTest {

    @Mock
    private FacturaService facturaService;

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private FacturaController facturaController;

    private Usuario usuario;
    private Cliente cliente;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        // Setup Usuario
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan@email.com");

        // Setup Cliente
        cliente = new Cliente();
        cliente.setClienteId(1);
        cliente.setUsuario(usuario);

        // Setup Pedido
        pedido = new Pedido();
        pedido.setId(1);
        pedido.setCliente(cliente);
        pedido.setFecha(LocalDate.now());
        pedido.setEstado("COMPLETADO");
        pedido.setTotal(56.0f);
        pedido.setDireccionEnvio("Av. Principal 123");
        pedido.setCostoEnvio(BigDecimal.valueOf(5.00));
        pedido.setMetodoPago("Efectivo");
        
        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDescargarFactura_Exitoso() {
        // Given
        Integer pedidoId = 1;
        byte[] pdfBytes = "PDF Content".getBytes();
        
        // Setup security context
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "juan@email.com", null, Arrays.asList(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(pedidoService.getPedidoById(pedidoId)).thenReturn(Optional.of(pedido));
        when(facturaService.puedeGenerarFactura(pedidoId)).thenReturn(true);
        when(facturaService.generarFacturaPDF(pedidoId)).thenReturn(pdfBytes);
        when(facturaService.generarNombreArchivo(pedidoId)).thenReturn("Factura_Pedido_1.pdf");

        // When
        ResponseEntity<byte[]> response = facturaController.descargarFactura(pedidoId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(pdfBytes, response.getBody());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("Factura_Pedido_1.pdf"));
        
        verify(facturaService).generarFacturaPDF(pedidoId);
    }

    @Test
    void testDescargarFactura_PedidoNoEncontrado() {
        // Given
        Integer pedidoId = 999;
        
        // Setup security context
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "juan@email.com", null, Arrays.asList(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        when(pedidoService.getPedidoById(pedidoId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<byte[]> response = facturaController.descargarFactura(pedidoId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(facturaService, never()).generarFacturaPDF(anyInt());
    }

    @Test
    void testDescargarFactura_UsuarioNoAutenticado() {
        // Given
        Integer pedidoId = 1;
        
        // No authentication context
        SecurityContextHolder.clearContext();

        // When
        ResponseEntity<byte[]> response = facturaController.descargarFactura(pedidoId);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(facturaService, never()).generarFacturaPDF(anyInt());
    }
}