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
    
    /**
     * Obtener todas las reseñas de un cliente específico
     */
    List<Resena> findByCliente(com.pasteleria.cordova.model.Cliente cliente);
    
    /**
     * Obtener las 10 reseñas más recientes con todas las relaciones cargadas
     */
    @Query("SELECT r FROM Resena r " +
            "JOIN FETCH r.cliente c " +
            "JOIN FETCH c.usuario u " +
            "JOIN FETCH r.producto p " +
            "ORDER BY r.fechaCreacion DESC")
    List<Resena> findTop10RecentWithAllRelations();

    /**
     * Obtener reseñas pendientes con todas las relaciones cargadas
     */
    @Query("SELECT r FROM Resena r " +
            "JOIN FETCH r.cliente c " +
            "JOIN FETCH c.usuario u " +
            "JOIN FETCH r.producto p " +
            "WHERE r.aprobada = false " +
            "ORDER BY r.fechaCreacion DESC")
    List<Resena> findPendientesWithAllRelations();

    /**
     * Obtener reseñas aprobadas con todas las relaciones cargadas
     */
    @Query("SELECT r FROM Resena r " +
            "JOIN FETCH r.cliente c " +
            "JOIN FETCH c.usuario u " +
            "JOIN FETCH r.producto p " +
            "WHERE r.aprobada = true " +
            "ORDER BY r.fechaCreacion DESC")
    List<Resena> findAprobadasWithAllRelations();

    /**
     * Obtener una reseña por ID con todas las relaciones cargadas
     */
    @Query("SELECT r FROM Resena r " +
            "JOIN FETCH r.cliente c " +
            "JOIN FETCH c.usuario u " +
            "JOIN FETCH r.producto p " +
            "WHERE r.id = :id")
    java.util.Optional<Resena> findByIdWithAllRelations(@org.springframework.data.repository.query.Param("id") Integer id);

}
