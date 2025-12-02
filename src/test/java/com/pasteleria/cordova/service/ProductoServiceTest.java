package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.repository.ProductoRepository;
import com.pasteleria.cordova.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTestSimple {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        producto1 = new Producto();
        producto1.setId(1);
        producto1.setNombre("Torta de Chocolate");
        producto1.setPrecio(25.50f);
        producto1.setStock(10);
        producto1.setCategoria("Tortas");

        producto2 = new Producto();
        producto2.setId(2);
        producto2.setNombre("Cupcake de Vainilla");
        producto2.setPrecio(5.00f);
        producto2.setStock(20);
        producto2.setCategoria("Cupcakes");
    }

    @Test
    void testFindAllProductos() {
        // Given
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoRepository.findAll()).thenReturn(productos);

        // When
        List<Producto> resultado = productoService.findAllProductos();

        // Then
        assertEquals(2, resultado.size());
        assertEquals("Torta de Chocolate", resultado.get(0).getNombre());
        assertEquals("Cupcake de Vainilla", resultado.get(1).getNombre());
        verify(productoRepository).findAll();
    }

    @Test
    void testFindById_Encontrado() {
        // Given
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto1));

        // When
        Optional<Producto> resultado = productoService.findById(1);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Torta de Chocolate", resultado.get().getNombre());
        verify(productoRepository).findById(1);
    }

    @Test
    void testFindById_NoEncontrado() {
        // Given
        when(productoRepository.findById(999)).thenReturn(Optional.empty());

        // When
        Optional<Producto> resultado = productoService.findById(999);

        // Then
        assertFalse(resultado.isPresent());
        verify(productoRepository).findById(999);
    }

    @Test
    void testSaveProducto() {
        // Given
        when(productoRepository.save(producto1)).thenReturn(producto1);

        // When
        Producto resultado = productoService.saveProducto(producto1);

        // Then
        assertEquals("Torta de Chocolate", resultado.getNombre());
        verify(productoRepository).save(producto1);
    }

    @Test
    void testDeleteProducto() {
        // Given
        Integer productoId = 1;

        // When
        productoService.deleteProducto(productoId);

        // Then
        verify(productoRepository).deleteById(productoId);
    }

    @Test
    void testSearchProductos() {
        // Given
        String searchTerm = "chocolate";
        List<Producto> productos = Arrays.asList(producto1);
        when(productoRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(searchTerm, searchTerm))
                .thenReturn(productos);

        // When
        List<Producto> resultado = productoService.searchProductos(searchTerm);

        // Then
        assertEquals(1, resultado.size());
        assertEquals("Torta de Chocolate", resultado.get(0).getNombre());
        verify(productoRepository).findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(searchTerm, searchTerm);
    }

    @Test
    void testFindByCategoria() {
        // Given
        String categoria = "Tortas";
        List<Producto> productos = Arrays.asList(producto1);
        when(productoRepository.findByCategoria(categoria)).thenReturn(productos);

        // When
        List<Producto> resultado = productoService.findByCategoria(categoria);

        // Then
        assertEquals(1, resultado.size());
        assertEquals("Torta de Chocolate", resultado.get(0).getNombre());
        verify(productoRepository).findByCategoria(categoria);
    }
}