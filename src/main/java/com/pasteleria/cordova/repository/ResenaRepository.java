package com.pasteleria.cordova.repository;

import com.pasteleria.cordova.model.Producto;
import com.pasteleria.cordova.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    List<Resena> findByProducto(Producto producto);

    List<Resena> findByAprobada(boolean aprobada);

    List<Resena> findByAprobadaOrderByFechaCreacionDesc(boolean aprobada);

    @Query("SELECT r FROM Resena r " +
            "JOIN FETCH r.cliente c " +
            "JOIN FETCH c.usuario u " +
            "JOIN FETCH r.producto p " +
            "WHERE r.aprobada = true")
    List<Resena> findAllApprovedWithClienteAndUsuarioAndProducto();
    
    // Métodos para notificaciones
    List<Resena> findByAprobadaFalseOrderByFechaCreacionDesc();
    List<Resena> findTop10ByOrderByFechaCreacionDesc();
    int countByAprobadaFalse();

    // =================== MÉTODOS PARA CLIENTES ===================
    
    /**
     * Verificar si existe una reseña de un cliente para un producto específico
     */
    boolean existsByClienteClienteIdAndProductoId(Integer clienteId, Integer productoId);
    
    /**
     * Obtener reseñas de un cliente específico ordenadas por fecha de creación
     */
    List<Resena> findByClienteClienteIdOrderByFechaCreacionDesc(Integer clienteId);
    
    /**
     * Obtener reseña específica de un cliente para un producto
     */
    @Query("SELECT r FROM Resena r WHERE r.cliente.clienteId = ?1 AND r.producto.id = ?2")
    java.util.Optional<Resena> findByClienteClienteIdAndProductoId(Integer clienteId, Integer productoId);

}
