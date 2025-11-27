package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.Pedido;
import com.pasteleria.cordova.model.Resena;
import com.pasteleria.cordova.repository.PedidoRepository;
import com.pasteleria.cordova.repository.ResenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificacionService {

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private ResenaRepository resenaRepository;
    

    
    /**
     * Obtiene todas las notificaciones (alertas) para el administrador
     * @return List de notificaciones con formato para mostrar en el dropdown
     */
    public List<Map<String, Object>> obtenerNotificacionesAlertas() {
        List<Map<String, Object>> notificaciones = new ArrayList<>();
        
        // Obtener pedidos nuevos (últimos 7 días)
        LocalDate hace7Dias = LocalDate.now().minusDays(7);
        List<Pedido> pedidosRecientes = pedidoRepository.findByFechaAfterOrderByFechaDesc(hace7Dias);
        
        for (Pedido pedido : pedidosRecientes) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("id", pedido.getId());
            notif.put("tipo", "pedido");
            notif.put("titulo", "Nuevo pedido #" + pedido.getId());
            notif.put("descripcion", "Pedido por $" + pedido.getTotal() + " - " + pedido.getEstado());
            notif.put("fecha", pedido.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            notif.put("icono", "fas fa-shopping-cart");
            notif.put("color", "bg-success");
            notif.put("url", "/admin/pedidos");
            notificaciones.add(notif);
        }
        
        // Obtener reseñas pendientes de aprobación
        List<Resena> resenasPendientes = resenaRepository.findTop10RecentWithAllRelations().stream()
                .filter(r -> !r.isAprobada())
                .collect(Collectors.toList());
        
        for (Resena resena : resenasPendientes) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("id", resena.getId());
            notif.put("tipo", "resena");
            notif.put("titulo", "Nueva reseña pendiente");
            notif.put("descripcion", "Reseña de " + resena.getProducto().getNombre() + " - " + resena.getCalificacion() + " estrellas");
            notif.put("fecha", resena.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            notif.put("icono", "fas fa-star");
            notif.put("color", "bg-warning");
            notif.put("url", "/admin/resenas");
            notificaciones.add(notif);
        }
        
        // Ordenar por fecha más reciente
        notificaciones.sort((a, b) -> {
            String fechaA = (String) a.get("fecha");
            String fechaB = (String) b.get("fecha");
            return fechaB.compareTo(fechaA);
        });
        
        // Limitar a las 5 más recientes
        return notificaciones.size() > 5 ? notificaciones.subList(0, 5) : notificaciones;
    }
    
    /**
     * Obtiene mensajes/reseñas para el dropdown de mensajes
     * @return List de mensajes con formato para mostrar
     */
    public List<Map<String, Object>> obtenerNotificacionesMensajes() {
        List<Map<String, Object>> mensajes = new ArrayList<>();
        
        // Obtener reseñas recientes (tanto aprobadas como pendientes)
        List<Resena> resenasRecientes = resenaRepository.findTop10RecentWithAllRelations().stream()
                .limit(10)
                .collect(Collectors.toList());
        
        for (Resena resena : resenasRecientes) {
            Map<String, Object> mensaje = new HashMap<>();
            mensaje.put("id", resena.getId());
            mensaje.put("tipo", "resena");
            mensaje.put("clienteNombre", resena.getCliente().getUsuario().getNombre());
            mensaje.put("contenido", limitarTexto(resena.getComentario(), 80));
            mensaje.put("fechaRelativa", calcularTiempoRelativo(resena.getFechaCreacion()));
            mensaje.put("calificacion", resena.getCalificacion());
            mensaje.put("producto", resena.getProducto().getNombre());
            mensaje.put("aprobada", resena.isAprobada());
            mensaje.put("url", "/admin/resenas");
            mensajes.add(mensaje);
        }
        
        return mensajes;
    }
    
    /**
     * Cuenta el total de notificaciones sin leer
     */
    public int contarNotificacionesPendientes() {
        // Contar pedidos de los últimos 7 días
        LocalDate hace7Dias = LocalDate.now().minusDays(7);
        int pedidosRecientes = pedidoRepository.countByFechaAfter(hace7Dias);
        
        // Contar reseñas pendientes
        int resenasPendientes = resenaRepository.countByAprobadaFalse();
        
        return pedidosRecientes + resenasPendientes;
    }
    
    /**
     * Cuenta el total de mensajes/reseñas
     */
    public int contarMensajesPendientes() {
        return resenaRepository.countByAprobadaFalse();
    }
    
    private String limitarTexto(String texto, int limite) {
        if (texto == null) return "";
        return texto.length() > limite ? texto.substring(0, limite) + "..." : texto;
    }
    
    private String calcularTiempoRelativo(LocalDateTime fecha) {
        LocalDateTime ahora = LocalDateTime.now();
        long minutos = java.time.Duration.between(fecha, ahora).toMinutes();
        
        if (minutos < 60) {
            return minutos + "m";
        } else if (minutos < 1440) { // 24 horas
            return (minutos / 60) + "h";
        } else {
            return (minutos / 1440) + "d";
        }
    }
}