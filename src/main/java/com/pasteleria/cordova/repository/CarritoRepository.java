package com.pasteleria.cordova.repository;


import com.pasteleria.cordova.model.Carrito;
import com.pasteleria.cordova.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {
    // Encontrar el carrito activo de un cliente
    Optional<Carrito> findByCliente(Cliente cliente);
}