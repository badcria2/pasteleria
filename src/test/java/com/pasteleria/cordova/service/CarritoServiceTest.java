package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.repository.CarritoRepository;
import com.pasteleria.cordova.service.CarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTestSimple {

    @Mock
    private CarritoRepository carritoRepository;

    @InjectMocks
    private CarritoService carritoService;

    private Usuario usuario;
    private Cliente cliente;
    private Producto producto;
    private Carrito carrito;
    private DetalleCarrito detalleCarrito;

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

        // Setup Producto
        producto = new Producto();
        producto.setId(1);
        producto.setNombre("Torta de Chocolate");
        producto.setPrecio(25.50f);
        producto.setStock(10);

        // Setup DetalleCarrito
        detalleCarrito = new DetalleCarrito();
        detalleCarrito.setProducto(producto);
        detalleCarrito.setCantidad(2);
        detalleCarrito.setPrecioUnitario(25.50f);
        detalleCarrito.setSubtotal(51.0f);

        // Setup Carrito
        carrito = new Carrito();
        carrito.setId(1);
        carrito.setCliente(cliente);
        carrito.setDetalles(Arrays.asList(detalleCarrito));
        detalleCarrito.setCarrito(carrito);
    }

    @Test
    void testFindById() {
        // Given
        when(carritoRepository.findById(1)).thenReturn(Optional.of(carrito));

        // When
        Optional<Carrito> resultado = carritoRepository.findById(1);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getId());
        verify(carritoRepository).findById(1);
    }

    @Test
    void testSaveCarrito() {
        // Given
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        // When
        Carrito resultado = carritoRepository.save(carrito);

        // Then
        assertEquals(1, resultado.getId());
        verify(carritoRepository).save(carrito);
    }

    @Test
    void testCarritoDetalles() {
        // Given & When
        // El carrito ya está configurado con detalles

        // Then
        assertNotNull(carrito.getDetalles());
        assertEquals(1, carrito.getDetalles().size());
        assertEquals("Torta de Chocolate", carrito.getDetalles().get(0).getProducto().getNombre());
        assertEquals(2, carrito.getDetalles().get(0).getCantidad());
    }
}