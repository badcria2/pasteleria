package com.pasteleria.cordova.repository;

import com.pasteleria.cordova.model.Cliente;
import com.pasteleria.cordova.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuario(Usuario usuario); // <--- Cambiado de Integer a Usuario
    
    /**
     * Obtener todos los clientes con sus pedidos y usuario cargados
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "LEFT JOIN FETCH c.pedidos p " +
           "JOIN FETCH c.usuario u " +
           "ORDER BY c.clienteId")
    List<Cliente> findAllWithPedidosAndUsuario();
    
    /**
     * Obtener un cliente por ID con sus pedidos y usuario cargados
     */
    @Query("SELECT c FROM Cliente c " +
           "LEFT JOIN FETCH c.pedidos p " +
           "JOIN FETCH c.usuario u " +
           "WHERE c.clienteId = :id")
    Optional<Cliente> findByIdWithPedidosAndUsuario(@Param("id") Integer id);
}