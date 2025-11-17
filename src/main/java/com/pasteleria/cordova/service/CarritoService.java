package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.repository.CarritoRepository;
import com.pasteleria.cordova.repository.DetalleCarritoRepository;
import com.pasteleria.cordova.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;
    @Autowired
    private DetalleCarritoRepository detalleCarritoRepository;
    @Autowired
    private ProductoRepository productoRepository; // Para verificar el producto y obtener su precio/stock

    // Obtener el carrito de un cliente (crea uno si no existe)
    @Transactional
    public Carrito getOrCreateCarrito(Cliente cliente) {
        return carritoRepository.findByCliente(cliente)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setCliente(cliente);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    // Añadir producto al carrito
    @Transactional
    public DetalleCarrito agregarProductoAlCarrito(Cliente cliente, Integer productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException("No hay suficiente stock para el producto: " + producto.getNombre());
        }

        Carrito carrito = getOrCreateCarrito(cliente);

        Optional<DetalleCarrito> existingDetalle = detalleCarritoRepository.findByCarritoAndProducto(carrito, producto);

        DetalleCarrito detalle;
        if (existingDetalle.isPresent()) {
            detalle = existingDetalle.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
        } else {
            detalle = new DetalleCarrito();
            detalle.setCarrito(carrito);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
        }
        detalle.setSubtotal(producto.getPrecio() * detalle.getCantidad());
        return detalleCarritoRepository.save(detalle);
    }

    // Actualizar cantidad de un producto en el carrito
    @Transactional
    public DetalleCarrito actualizarCantidadProducto(Cliente cliente, Integer productoId, int nuevaCantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
        Carrito carrito = getOrCreateCarrito(cliente);
        DetalleCarrito detalle = detalleCarritoRepository.findByCarritoAndProducto(carrito, producto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en el carrito."));

        if (nuevaCantidad <= 0) {
            detalleCarritoRepository.delete(detalle);
            return null; // Producto eliminado
        }

        if (producto.getStock() < nuevaCantidad) {
            throw new IllegalArgumentException("No hay suficiente stock para el producto: " + producto.getNombre());
        }

        detalle.setCantidad(nuevaCantidad);
        detalle.setSubtotal(producto.getPrecio() * detalle.getCantidad());
        return detalleCarritoRepository.save(detalle);
    }

    // Eliminar producto del carrito
    @Transactional
    public void eliminarProductoDelCarrito(Cliente cliente, Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
        Carrito carrito = getOrCreateCarrito(cliente);
        DetalleCarrito detalle = detalleCarritoRepository.findByCarritoAndProducto(carrito, producto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en el carrito."));
        detalleCarritoRepository.delete(detalle);
    }

    // Obtener todos los detalles del carrito de un cliente
    public List<DetalleCarrito> getDetallesCarrito(Cliente cliente) {
        Carrito carrito = getOrCreateCarrito(cliente);
        return detalleCarritoRepository.findByCarrito(carrito);
    }

    // Calcular el total del carrito
    public double calcularTotalCarrito(Cliente cliente) {
        return getDetallesCarrito(cliente).stream()
                .mapToDouble(DetalleCarrito::getSubtotal)
                .sum();
    }

    // Vaciar el carrito
    @Transactional
    public void vaciarCarrito(Cliente cliente) {
        Carrito carrito = getOrCreateCarrito(cliente);
        detalleCarritoRepository.deleteByCarrito(carrito);
    }
}