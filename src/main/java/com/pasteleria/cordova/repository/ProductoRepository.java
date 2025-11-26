package com.pasteleria.cordova.repository;


import com.pasteleria.cordova.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // Método para buscar productos por nombre o descripción, ignorando mayúsculas/minúsculas
    List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String nombre, String descripcion);

    List<Producto> findByCategoria(String categoria);

    // Consulta para obtener los productos más vendidos
    @Query(value = "SELECT p.nombre as nombreProducto, SUM(dp.cantidad) as totalVendido " +
           "FROM producto p " +
           "INNER JOIN detalle_pedido dp ON p.id = dp.producto_id " +
           "INNER JOIN pedido pe ON dp.pedido_id = pe.id " +
           "WHERE pe.estado IN ('COMPLETADO', 'EN_PROCESO') " +
           "GROUP BY p.id, p.nombre " +
           "ORDER BY totalVendido DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findProductosMasVendidos(@Param("limit") int limit);

}