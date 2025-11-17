package com.pasteleria.cordova.repository;


import com.pasteleria.cordova.model.Boleta;
import com.pasteleria.cordova.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Integer> {
    Optional<Boleta> findByPedido(Pedido pedido);
}