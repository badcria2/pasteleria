package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.*;
import com.pasteleria.cordova.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // Importar BigDecimal para el total y costo de envío
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
    public Pedido crearPedidoDesdeCarrito(Usuario usuario, String metodoPago, String direccionEnvio, BigDecimal costoEnvio) {
        // 1. Obtener el carrito del cliente con sus detalles
        Carrito carrito = carritoService.getCarritoByCliente(usuario);
        if (carrito.getDetalles().isEmpty()) {
            throw new RuntimeException("El carrito del cliente está vacío, no se puede crear un pedido.");
        }

        // 2. Crear el nuevo Pedido
        Pedido nuevoPedido = new Pedido();
        Cliente cliente = clienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado para el usuario: " + usuario.getEmail()));
        nuevoPedido.setCliente(cliente);
        nuevoPedido.setFecha(LocalDate.now()); // O LocalDateTime.now() si tu campo es LocalDateTime

        nuevoPedido.setEstado("PENDIENTE");
        nuevoPedido.setMetodoPago(metodoPago);
        nuevoPedido.setDireccionEnvio(direccionEnvio); // <-- Asignar la dirección de envío
        nuevoPedido.setCostoEnvio(costoEnvio);         // <-- Asignar el costo de envío

        BigDecimal subtotalPedido = BigDecimal.ZERO; // Usar BigDecimal para cálculos monetarios
        List<DetallePedido> detallesDelPedido = new ArrayList<>();

        // 3. Convertir DetalleCarrito a DetallePedido y calcular subtotal
        for (DetalleCarrito dc : carrito.getDetalles()) {
            DetallePedido dp = new DetallePedido();
            dp.setPedido(nuevoPedido);
            dp.setProducto(dc.getProducto());
            dp.setCantidad(dc.getCantidad());
            dp.setPrecioUnitario(dc.getPrecioUnitario()); // Asumo que getPrecioUnitario() devuelve BigDecimal o float/double

            // Calcular subtotal del detalle usando BigDecimal
            BigDecimal subTotalDetalle = BigDecimal.valueOf(dc.getPrecioUnitario()).multiply(BigDecimal.valueOf(dc.getCantidad()));
            dp.setSubTotal(subTotalDetalle.floatValue()); // Convertir a float si tu campo SubTotal es float

            detallesDelPedido.add(dp);
            subtotalPedido = subtotalPedido.add(subTotalDetalle); // Sumar al subtotal general
        }

        // Calcular el total final (subtotal del carrito + costo de envío)
        BigDecimal totalFinal = subtotalPedido.add(costoEnvio);
        nuevoPedido.setTotal(totalFinal.floatValue()); // Asignar el total final al pedido principal (convertir a float si es necesario)
        nuevoPedido.setDetalles(detallesDelPedido);

        // 4. Guardar el Pedido
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

    // Método para obtener todos los pedidos (útil para la vista de administrador)
    public List<Pedido> findAllPedidos() {
        return pedidoRepository.findAll();
    }

    // Método para obtener pedidos filtrados por estado
    public List<Pedido> findPedidosByEstado(String estado) {
        if (estado == null || estado.trim().isEmpty() || estado.equals("TODOS")) {
            return pedidoRepository.findAllWithAllRelations();
        }
        return pedidoRepository.findByEstadoWithAllRelations(estado);
    }

    // Método para obtener el conteo de pedidos por estado
    public long countPedidosByEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return pedidoRepository.count();
        }
        return pedidoRepository.countByEstado(estado);
    }



    @Transactional
    public List<Pedido> getPedidosByClienteWithDetalles(Usuario usuario) {
        Cliente cliente = clienteRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado para el usuario: " + usuario.getEmail()));
        return pedidoRepository.findByClienteWithDetallesAndProductosOrderByFechaPedidoDesc(cliente);
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> getPedidoByIdWithDetalles(Integer id) {
        return pedidoRepository.findByIdWithDetallesAndProductos(id);
    }

    // Métodos para el dashboard
    public BigDecimal getTotalVentas() {
        List<Pedido> pedidosCompletados = pedidoRepository.findByEstado("COMPLETADO");
        double suma = pedidosCompletados.stream()
                .mapToDouble(pedido -> pedido.getTotal() != null ? pedido.getTotal() : 0.0)
                .sum();
        return BigDecimal.valueOf(suma);
    }

    public BigDecimal getVentasDelMes() {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = inicioMes.plusMonths(1).minusDays(1);
        
        List<Pedido> pedidosDelMes = pedidoRepository.findAll().stream()
                .filter(p -> p.getFecha() != null && 
                           !p.getFecha().isBefore(inicioMes) && 
                           !p.getFecha().isAfter(finMes) &&
                           "COMPLETADO".equals(p.getEstado()))
                .collect(Collectors.toList());
                
        double suma = pedidosDelMes.stream()
                .mapToDouble(pedido -> pedido.getTotal() != null ? pedido.getTotal() : 0.0)
                .sum();
        return BigDecimal.valueOf(suma);
    }

    public long getTotalClientes() {
        return clienteRepository.count();
    }

    public long getTotalPedidos() {
        return pedidoRepository.count();
    }

    public List<Double> getVentasPorMes() {
        List<Double> ventasPorMes = new ArrayList<>();
        LocalDate fechaActual = LocalDate.now();
        
        for (int i = 11; i >= 0; i--) {
            LocalDate inicioMes = fechaActual.minusMonths(i).withDayOfMonth(1);
            LocalDate finMes = inicioMes.plusMonths(1).minusDays(1);
            
            List<Pedido> pedidosDelMes = pedidoRepository.findAll().stream()
                    .filter(p -> p.getFecha() != null && 
                               !p.getFecha().isBefore(inicioMes) && 
                               !p.getFecha().isAfter(finMes) &&
                               "COMPLETADO".equals(p.getEstado()))
                    .collect(Collectors.toList());
                    
            double suma = pedidosDelMes.stream()
                    .mapToDouble(pedido -> pedido.getTotal() != null ? pedido.getTotal() : 0.0)
                    .sum();
            
            ventasPorMes.add(suma);
        }
        
        return ventasPorMes;
    }
    
    public List<String> getEtiquetasMeses() {
        List<String> etiquetas = new ArrayList<>();
        String[] mesesNombres = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", 
                                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        LocalDate fechaActual = LocalDate.now();
        
        for (int i = 11; i >= 0; i--) {
            LocalDate mes = fechaActual.minusMonths(i);
            etiquetas.add(mesesNombres[mes.getMonthValue() - 1]);
        }
        
        return etiquetas;
    }
}