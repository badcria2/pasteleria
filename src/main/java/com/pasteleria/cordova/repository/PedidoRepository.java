package com.pasteleria.cordova.repository;

import com.pasteleria.cordova.model.Cliente;
import com.pasteleria.cordova.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByCliente(Cliente cliente);
    List<Pedido> findByEstado(String estado); // Ejemplo: para buscar pedidos 'pendientes'
    long countByEstado(String estado); // Conteo de pedidos por estado
    List<Pedido> findByClienteOrderByFechaDesc(Cliente cliente);
    
    // Métodos para el panel de administración con fetch JOIN
    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.cliente c LEFT JOIN FETCH c.usuario u LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto pr WHERE p.estado = :estado ORDER BY p.fecha DESC")
    List<Pedido> findByEstadoWithAllRelations(@Param("estado") String estado);
    
    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.cliente c LEFT JOIN FETCH c.usuario u LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.producto pr ORDER BY p.fecha DESC")
    List<Pedido> findAllWithAllRelations();
    @Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.detalles dp JOIN FETCH dp.producto pr WHERE p.cliente = :cliente ORDER BY p.fecha DESC")
    List<Pedido> findByClienteWithDetallesAndProductosOrderByFechaPedidoDesc(@Param("cliente") Cliente cliente);

    // Opcional: si tienes un solo pedido por ID y necesitas los detalles
    @Query("SELECT p FROM Pedido p JOIN FETCH p.detalles dp JOIN FETCH dp.producto pr WHERE p.id = :id")
    Optional<Pedido> findByIdWithDetallesAndProductos(@Param("id") Integer id);
    
    // Métodos para notificaciones
    List<Pedido> findByFechaAfterOrderByFechaDesc(LocalDate fecha);
    int countByFechaAfter(LocalDate fecha);
}