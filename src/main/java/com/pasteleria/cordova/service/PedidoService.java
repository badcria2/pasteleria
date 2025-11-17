package com.pasteleria.cordova.service;

import com.google.common.base.Preconditions; // Ejemplo de uso de Guava
import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.repository.DetallePedidoRepository;
import com.pasteleria.cordova.repository.PedidoRepository;
import com.pasteleria.cordova.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private DetallePedidoRepository detallePedidoRepository;
    @Autowired
    private CarritoService carritoService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private BoletaService boletaService; // Para generar la boleta automáticamente

    @Transactional
    public Pedido crearPedidoDesdeCarrito(Cliente cliente, String metodoPago) {
        Preconditions.checkNotNull(cliente, "El cliente no puede ser nulo."); // Ejemplo de Guava
        Preconditions.checkArgument(!carritoService.getDetallesCarrito(cliente).isEmpty(), "El carrito está vacío.");

        List<DetalleCarrito> detallesCarrito = carritoService.getDetallesCarrito(cliente);

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFecha(LocalDate.now());
        pedido.setEstado("PENDIENTE"); // Estado inicial del pedido
        pedido.setMetodoPago(metodoPago);

        Pedido savedPedido = pedidoRepository.save(pedido); // Primero guardamos el pedido para obtener su ID

        Float totalPedido = 0.0f;

        for (DetalleCarrito dc : detallesCarrito) {
            Producto producto = dc.getProducto();

            // Verificar stock antes de confirmar el pedido
            if (producto.getStock() < dc.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // Reducir stock del producto
            producto.setStock(producto.getStock() - dc.getCantidad());
            productoService.saveProducto(producto); // Actualizar el producto en la BD

            DetallePedido dp = new DetallePedido();
            dp.setPedido(savedPedido);
            dp.setProducto(producto);
            dp.setCantidad(dc.getCantidad());
            dp.setPrecioUnitario(producto.getPrecio());
            dp.setSubTotal(dc.getSubtotal());
            detallePedidoRepository.save(dp);

            totalPedido += dc.getSubtotal();
        }

        savedPedido.setTotal(totalPedido);
        pedidoRepository.save(savedPedido); // Actualizar el total del pedido

        // Generar boleta automáticamente al crear el pedido
        boletaService.generarBoletaParaPedido(savedPedido);

        carritoService.vaciarCarrito(cliente); // Vaciar el carrito después de crear el pedido
        return savedPedido;
    }

    public List<Pedido> findPedidosByCliente(Cliente cliente) {
        return pedidoRepository.findByCliente(cliente);
    }

    public Optional<Pedido> findById(Integer pedidoId) {
        return pedidoRepository.findById(pedidoId);
    }

    public List<DetallePedido> findDetallesByPedido(Pedido pedido) {
        return detallePedidoRepository.findByPedido(pedido);
    }

    public List<Pedido> findAllPedidos() {
        return pedidoRepository.findAll();
    }

    // Actualizar el estado de un pedido (para el administrador)
    @Transactional
    public Pedido actualizarEstadoPedido(Integer pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));

        // Aquí podrías agregar lógica de validación para los estados
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    // Otros métodos de negocio para pedidos
}