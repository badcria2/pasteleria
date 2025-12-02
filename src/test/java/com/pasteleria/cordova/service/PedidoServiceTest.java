package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.repository.PedidoRepository;
import com.pasteleria.cordova.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTestFinal {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoService pedidoService;

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
    }

    @Test
    void testGetPedidoById_Encontrado() {
        // Given
        when(pedidoRepository.findById(1)).thenReturn(Optional.of(pedido));

        // When
        Optional<Pedido> resultado = pedidoService.getPedidoById(1);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("COMPLETADO", resultado.get().getEstado());
        verify(pedidoRepository).findById(1);
    }

    @Test
    void testGetPedidoById_NoEncontrado() {
        // Given
        when(pedidoRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Pedido> resultado = pedidoService.getPedidoById(999);

        // Then
        assertFalse(resultado.isPresent());
        verify(pedidoRepository).findById(999);
    }

    @Test
    void testFindAllPedidos() {
        // Given
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        // When
        List<Pedido> resultado = pedidoService.findAllPedidos();

        // Then
        assertEquals(1, resultado.size());
        assertEquals("COMPLETADO", resultado.get(0).getEstado());
        verify(pedidoRepository).findAll();
    }

    @Test
    void testFindPedidosByEstado() {
        // Given
        String estado = "COMPLETADO";
        List<Pedido> pedidos = Arrays.asList(pedido);
        when(pedidoRepository.findByEstadoWithAllRelations(estado)).thenReturn(pedidos);

        // When
        List<Pedido> resultado = pedidoService.findPedidosByEstado(estado);

        // Then - Verifica que el método se ejecuta correctamente
        assertNotNull(resultado);
        verify(pedidoRepository).findByEstadoWithAllRelations(estado);
    }

    @Test
    void testCountPedidosByEstado() {
        // Given
        String estado = "COMPLETADO";
        when(pedidoRepository.countByEstado(estado)).thenReturn(5L);

        // When
        long resultado = pedidoService.countPedidosByEstado(estado);

        // Then
        assertEquals(5L, resultado);
        verify(pedidoRepository).countByEstado(estado);
    }
}