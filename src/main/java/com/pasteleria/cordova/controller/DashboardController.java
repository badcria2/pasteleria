package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.service.PedidoService;
import com.pasteleria.cordova.service.ProductoService;
import com.pasteleria.cordova.service.NotificacionService;
import com.pasteleria.cordova.model.Pedido;
import com.pasteleria.cordova.model.Producto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Obtener estadísticas usando los métodos del servicio
        BigDecimal totalVentas = pedidoService.getTotalVentas();
        BigDecimal ventasDelMes = pedidoService.getVentasDelMes();
        long totalClientes = pedidoService.getTotalClientes();
        long totalPedidos = pedidoService.getTotalPedidos();
        
        // Contar pedidos por estado usando strings
        List<Pedido> todosPedidos = pedidoService.findAllPedidos();
        Map<String, Long> pedidosPorEstado = todosPedidos.stream()
                .collect(Collectors.groupingBy(Pedido::getEstado, Collectors.counting()));
        
        long totalPendientes = pedidosPorEstado.getOrDefault("PENDIENTE", 0L);
        long totalEnProceso = pedidosPorEstado.getOrDefault("EN_PROCESO", 0L);
        long totalCompletados = pedidosPorEstado.getOrDefault("COMPLETADO", 0L);
        long totalCancelados = pedidosPorEstado.getOrDefault("CANCELADO", 0L);
        
        // Obtener productos para mostrar
        List<Producto> todosLosProductos = productoService.findAllProductos();
        int productosConPocoStock = (int) todosLosProductos.stream()
            .filter(p -> p.getStock() < 10)
            .count();
        
        // Agregar datos al modelo
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("ventasDelMes", ventasDelMes);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("totalPendientes", totalPendientes);
        model.addAttribute("totalEnProceso", totalEnProceso);
        model.addAttribute("totalCompletados", totalCompletados);
        model.addAttribute("totalCancelados", totalCancelados);
        model.addAttribute("totalProductos", todosLosProductos.size());
        model.addAttribute("productosConPocoStock", productosConPocoStock);
        
        // Obtener productos más vendidos
        List<Object[]> productosMasVendidos = productoService.getProductosMasVendidos(5);
        model.addAttribute("productosMasVendidos", productosMasVendidos);
        
        // Obtener ventas por mes para el gráfico
        List<Double> ventasPorMes = pedidoService.getVentasPorMes();
        List<String> etiquetasMeses = pedidoService.getEtiquetasMeses();
        model.addAttribute("ventasPorMes", ventasPorMes);
        model.addAttribute("etiquetasMeses", etiquetasMeses);
        
        // Agregar notificaciones para la topbar
        List<Map<String, Object>> notificacionesAlertas = notificacionService.obtenerNotificacionesAlertas();
        List<Map<String, Object>> notificacionesMensajes = notificacionService.obtenerNotificacionesMensajes();
        model.addAttribute("notificacionesAlertas", notificacionesAlertas);
        model.addAttribute("notificacionesMensajes", notificacionesMensajes);
        
        return "admin/dashboard";
    }
}