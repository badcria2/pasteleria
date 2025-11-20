package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.repository.CarritoRepository;
import com.pasteleria.cordova.repository.ClienteRepository;
import com.pasteleria.cordova.repository.DetalleCarritoRepository;
import com.pasteleria.cordova.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;
    @Autowired
    private DetalleCarritoRepository detalleCarritoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoRepository productoRepository;

    @Transactional
    public Carrito getCarritoByCliente(Usuario usuario) {
        Cliente cliente = clienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado para el usuario: " + usuario.getEmail()));
        // Aseguramos que los detalles del carrito se carguen al obtener el carrito
        return carritoRepository.findByCliente(cliente)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setCliente(cliente);
                    nuevoCarrito.setFechaCreacion(LocalDateTime.now());
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    @Transactional
    public Carrito addProductoToCarrito(Usuario usuario, Integer productoId, Integer cantidad) {
        Carrito carrito = getCarritoByCliente(usuario);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Optional<DetalleCarrito> existingDetalle = detalleCarritoRepository.findByCarritoAndProducto(carrito, producto);

        if (existingDetalle.isPresent()) {
            DetalleCarrito detalle = existingDetalle.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
            detalle.setSubtotal(detalle.getPrecioUnitario() * detalle.getCantidad()); // Actualizar subtotal
            detalleCarritoRepository.save(detalle);
        } else {
            DetalleCarrito nuevoDetalle = new DetalleCarrito();
            nuevoDetalle.setCarrito(carrito);
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(cantidad);
            nuevoDetalle.setPrecioUnitario(producto.getPrecio());
            nuevoDetalle.setSubtotal(producto.getPrecio() * cantidad); // ASIGNAR SUBTOTAL AQUÍ
            detalleCarritoRepository.save(nuevoDetalle);
        }
        // Recargar el carrito para asegurar que los detalles estén actualizados
        // Considera si realmente necesitas recargar todo el carrito o solo los detalles para el retorno.
        // Si tienes FetchType.LAZY en los detalles del Carrito, puede que necesites un findById del carrito.
        return carritoRepository.findByCliente(carrito.getCliente()).get();
    }

    @Transactional
    public Carrito updateProductoQuantity(Usuario usuario, Integer productoId, Integer cantidad) {
        Carrito carrito = getCarritoByCliente(usuario);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        DetalleCarrito detalle = detalleCarritoRepository.findByCarritoAndProducto(carrito, producto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));

        if (cantidad <= 0) {
            detalleCarritoRepository.delete(detalle);
        } else {
            detalle.setCantidad(cantidad);
            detalle.setSubtotal(detalle.getPrecioUnitario() * detalle.getCantidad()); // Actualizar subtotal
            detalleCarritoRepository.save(detalle);
        }
        return carritoRepository.findByCliente(carrito.getCliente()).get();
    }

    @Transactional
    public Carrito removeProductoFromCarrito(Usuario usuario, Integer productoId) {
        Carrito carrito = getCarritoByCliente(usuario);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        DetalleCarrito detalle = detalleCarritoRepository.findByCarritoAndProducto(carrito, producto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));

        detalleCarritoRepository.delete(detalle);
        return carritoRepository.findByCliente(carrito.getCliente()).get();
    }

    @Transactional
    public void clearCarrito(Usuario usuario) {
        Carrito carrito = getCarritoByCliente(usuario);
        detalleCarritoRepository.deleteByCarrito(carrito);
        // Para asegurar que la colección de detalles en el objeto Carrito esté vacía
        carrito.getDetalles().clear();
        carritoRepository.save(carrito); // Guarda el carrito sin detalles
    }

    public List<DetalleCarrito> getDetallesCarrito(Usuario usuario) {
        Carrito carrito = getCarritoByCliente(usuario);
        // Si los detalles del carrito están en FetchType.LAZY en la entidad Carrito,
        // al acceder a getDetalles() aquí, se cargarán si la sesión de JPA está abierta.
        // Asegúrate de que este método se llame dentro de un contexto transaccional
        // o que tu configuración de OpenSessionInView esté habilitada si lo necesitas en el controlador.
        return carrito.getDetalles();
    }
}