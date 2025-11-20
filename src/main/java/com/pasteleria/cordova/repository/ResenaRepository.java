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

}
