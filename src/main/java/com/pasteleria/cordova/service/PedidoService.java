package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private DetallePedidoRepository detallePedidoRepository;
    @Autowired
    private CarritoService carritoService; // Para interactuar con el carrito
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private BoletaRepository boletaRepository; // Si usas la entidad Boleta
    @Autowired
    private ProductoRepository productoRepository; // Para actualizar el stock

    @Transactional
    public Pedido crearPedidoDesdeCarrito(Usuario usuario, String metodoPago) {
        // 1. Obtener el carrito del cliente con sus detalles
        Carrito carrito = carritoService.getCarritoByCliente(usuario); // Este ya carga los detalles
        if (carrito.getDetalles().isEmpty()) {
            throw new RuntimeException("El carrito del cliente está vacío, no se puede crear un pedido.");
        }

        // 2. Crear el nuevo Pedido
        Pedido nuevoPedido = new Pedido();
        Cliente cliente = clienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado para el usuario: " + usuario.getEmail()));
        nuevoPedido.setCliente(cliente);
        nuevoPedido.setFecha(LocalDate.now());

        nuevoPedido.setEstado("PENDIENTE"); // O el estado inicial que desees
        nuevoPedido.setMetodoPago(metodoPago);

        Float totalPedido = 0.0f;
        List<DetallePedido> detallesDelPedido = new ArrayList<>();

        // 3. Convertir DetalleCarrito a DetallePedido y calcular subtotal
        for (DetalleCarrito dc : carrito.getDetalles()) {
            DetallePedido dp = new DetallePedido();
            dp.setPedido(nuevoPedido); // Asignar el pedido al detalle
            dp.setProducto(dc.getProducto());
            dp.setCantidad(dc.getCantidad());
            dp.setPrecioUnitario(dc.getPrecioUnitario());

            // *** ¡AQUÍ ESTÁ LA CLAVE PARA ELIMINAR EL ERROR! ***
            Float subTotalDetalle = dc.getPrecioUnitario() * dc.getCantidad();
            dp.setSubTotal(subTotalDetalle); // Asignar el subTotal calculado

            detallesDelPedido.add(dp);
            totalPedido += subTotalDetalle;
        }

        nuevoPedido.setTotal(totalPedido); // Asignar el total al pedido principal
        nuevoPedido.setDetalles(detallesDelPedido); // Asignar los detalles al pedido

        // 4. Guardar el Pedido (esto debería guardar también los DetallePedido debido a CascadeType.ALL)
        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);

        // 5. Limpiar el carrito después de crear el pedido
        carritoService.clearCarrito(usuario);

        return pedidoGuardado;
    }


    @Transactional
    public Pedido actualizarEstadoPedido(Integer pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> getPedidosByCliente(Usuario usuario) {
        Cliente cliente = clienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado para el usuario: " + usuario.getEmail()));
        return pedidoRepository.findByClienteOrderByFechaDesc(cliente);
    }

    public Optional<Pedido> getPedidoById(Integer pedidoId) {
        return pedidoRepository.findById(pedidoId);
    }

    @Transactional // Es crucial que este método sea transaccional para que el JOIN FETCH funcione
    public List<Pedido> getPedidosByClienteWithDetalles(Usuario usuario) {
        Cliente cliente = clienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado para el usuario: " + usuario.getEmail()));
        // Usamos el nuevo método con JOIN FETCH
        return pedidoRepository.findByClienteWithDetallesAndProductosOrderByFechaPedidoDesc(cliente);
    }
}