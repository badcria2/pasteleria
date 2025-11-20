package com.pasteleria.cordova.repository;

import com.pasteleria.cordova.model.Cliente;
import com.pasteleria.cordova.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByCliente(Cliente cliente);
    List<Pedido> findByEstado(String estado); // Ejemplo: para buscar pedidos 'pendientes'
    List<Pedido> findByClienteOrderByFechaDesc(Cliente cliente);
    @Query("SELECT p FROM Pedido p JOIN FETCH p.detalles dp JOIN FETCH dp.producto pr WHERE p.cliente = :cliente ORDER BY p.fecha DESC")
    List<Pedido> findByClienteWithDetallesAndProductosOrderByFechaPedidoDesc(@Param("cliente") Cliente cliente);

    // Opcional: si tienes un solo pedido por ID y necesitas los detalles
    @Query("SELECT p FROM Pedido p JOIN FETCH p.detalles dp JOIN FETCH dp.producto pr WHERE p.id = :id")
    Optional<Pedido> findByIdWithDetallesAndProductos(@Param("id") Integer id);
}