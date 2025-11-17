package com.pasteleria.cordova.repository;

import com.pasteleria.cordova.model.Cliente;
import com.pasteleria.cordova.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByCliente(Cliente cliente);
    List<Pedido> findByEstado(String estado); // Ejemplo: para buscar pedidos 'pendientes'
}