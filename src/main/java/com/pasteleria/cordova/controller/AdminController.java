package com.pasteleria.cordova.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.pasteleria.cordova.model.Pedido;
import com.pasteleria.cordova.model.Resena;
import com.pasteleria.cordova.model.Cliente;
import com.pasteleria.cordova.service.PedidoService;
import com.pasteleria.cordova.service.ResenaService;
import com.pasteleria.cordova.service.ClienteService;
import com.pasteleria.cordova.service.NotificacionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final PedidoService pedidoService;
    private final ResenaService resenaService;
    private final ClienteService clienteService;
    private final NotificacionService notificacionService;
    
    public AdminController(PedidoService pedidoService, ResenaService resenaService, ClienteService clienteService, NotificacionService notificacionService) {
        this.pedidoService = pedidoService;
        this.resenaService = resenaService;
        this.clienteService = clienteService;
        this.notificacionService = notificacionService;
    }
    
    @GetMapping("/pedidos/{id}/details")
    @ResponseBody
    public ResponseEntity<Pedido> getPedidoDetails(@PathVariable Integer id) {
        try {
            return pedidoService.getPedidoById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ===== GESTIÓN DE RESEÑAS =====
    
    @GetMapping("/resenas")
    @Transactional(readOnly = true)
    public String mostrarResenas(Model model) {
        try {
            List<Resena> resenasPendientes = resenaService.findResenasPendientes();
            List<Resena> resenasAprobadas = resenaService.findResenasAprobadas();
            
            // Agregar notificaciones para la topbar
            List<Map<String, Object>> notificacionesAlertas = notificacionService.obtenerNotificacionesAlertas();
            List<Map<String, Object>> notificacionesMensajes = notificacionService.obtenerNotificacionesMensajes();
            
            model.addAttribute("resenasPendientes", resenasPendientes);
            model.addAttribute("resenasAprobadas", resenasAprobadas);
            model.addAttribute("totalPendientes", resenasPendientes.size());
            model.addAttribute("totalAprobadas", resenasAprobadas.size());
            model.addAttribute("notificacionesAlertas", notificacionesAlertas);
            model.addAttribute("notificacionesMensajes", notificacionesMensajes);
            
            return "admin/resenas";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar las reseñas: " + e.getMessage());
            return "admin/resenas";
        }
    }
    
    @PostMapping("/resenas/{id}/aprobar")
    public String aprobarResena(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = resenaService.aprobarResena(id);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Reseña aprobada exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No se pudo aprobar la reseña.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al aprobar la reseña: " + e.getMessage());
        }
        return "redirect:/admin/resenas";
    }
    
    @PostMapping("/resenas/{id}/rechazar")
    public String rechazarResena(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = resenaService.eliminarResena(id);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Reseña rechazada y eliminada exitosamente.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No se pudo rechazar la reseña.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al rechazar la reseña: " + e.getMessage());
        }
        return "redirect:/admin/resenas";
    }
    
    // API para obtener detalles de reseña
    @GetMapping("/resenas/{id}/details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getResenaDetails(@PathVariable Integer id) {
        try {
            Optional<Resena> resenaOpt = resenaService.findById(id);
            if (resenaOpt.isPresent()) {
                Resena resena = resenaOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", resena.getId());
                response.put("clienteNombre", resena.getCliente().getUsuario().getNombre());
                response.put("clienteEmail", resena.getCliente().getUsuario().getEmail());
                response.put("productoNombre", resena.getProducto().getNombre());
                response.put("calificacion", resena.getCalificacion());
                response.put("comentario", resena.getComentario());
                response.put("fechaCreacion", resena.getFechaCreacion());
                response.put("aprobada", resena.isAprobada());
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ===== REPORTE DE CLIENTES =====
    
    @GetMapping("/clientes")
    @Transactional(readOnly = true)
    public String mostrarReporteClientes(Model model) {
        try {
            List<Cliente> clientes = clienteService.findAllClientes();
            
            // Calcular estadísticas
            int totalClientes = clientes.size();
            
            // Contar clientes con pedidos
            long clientesConPedidos = clientes.stream()
                .filter(cliente -> cliente.getPedidos() != null && !cliente.getPedidos().isEmpty())
                .count();
            
            // Calcular clientes registrados este mes (ejemplo)
            LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            long clientesNuevos = clientes.stream()
                .filter(cliente -> cliente.getUsuario().getFechaRegistro() != null && 
                        cliente.getUsuario().getFechaRegistro().isAfter(inicioMes))
                .count();
            
            // Agregar notificaciones para la topbar
            List<Map<String, Object>> notificacionesAlertas = notificacionService.obtenerNotificacionesAlertas();
            List<Map<String, Object>> notificacionesMensajes = notificacionService.obtenerNotificacionesMensajes();
            
            model.addAttribute("clientes", clientes);
            model.addAttribute("totalClientes", totalClientes);
            model.addAttribute("clientesConPedidos", clientesConPedidos);
            model.addAttribute("clientesNuevos", clientesNuevos);
            model.addAttribute("porcentajeConPedidos", 
                totalClientes > 0 ? (clientesConPedidos * 100.0 / totalClientes) : 0);
            model.addAttribute("notificacionesAlertas", notificacionesAlertas);
            model.addAttribute("notificacionesMensajes", notificacionesMensajes);
            
            return "admin/clientes";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el reporte de clientes: " + e.getMessage());
            return "admin/clientes";
        }
    }
    
    // API para obtener estadísticas detalladas de cliente
    @GetMapping("/clientes/{id}/estadisticas")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getClienteEstadisticas(@PathVariable Integer id) {
        try {
            Optional<Cliente> clienteOpt = clienteService.findByIdWithPedidosAndUsuario(id);
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                Map<String, Object> stats = new HashMap<>();
                
                // Estadísticas básicas
                stats.put("nombreCompleto", cliente.getUsuario().getNombre());
                stats.put("email", cliente.getUsuario().getEmail());
                stats.put("telefono", cliente.getUsuario().getTelefono());
                stats.put("direccion", cliente.getDireccion());
                stats.put("fechaRegistro", cliente.getUsuario().getFechaRegistro());
                
                // Estadísticas de pedidos
                int totalPedidos = cliente.getPedidos() != null ? cliente.getPedidos().size() : 0;
                double montoTotal = cliente.getPedidos() != null ? 
                    cliente.getPedidos().stream().mapToDouble(p -> p.getTotal().doubleValue()).sum() : 0.0;
                
                stats.put("totalPedidos", totalPedidos);
                stats.put("montoTotalCompras", montoTotal);
                stats.put("promedioCompra", totalPedidos > 0 ? montoTotal / totalPedidos : 0.0);
                
                // Estadísticas de reseñas
                List<Resena> resenasCliente = resenaService.findByCliente(cliente);
                stats.put("totalResenas", resenasCliente.size());
                double promedioCalificaciones = resenasCliente.stream()
                    .mapToInt(Resena::getCalificacion)
                    .average()
                    .orElse(0.0);
                stats.put("promedioCalificaciones", promedioCalificaciones);
                
                return ResponseEntity.ok(stats);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
