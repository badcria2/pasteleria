package com.pasteleria.cordova.repository;

import com.pasteleria.cordova.model.Producto;
import com.pasteleria.cordova.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {
    List<Resena> findByProducto(Producto producto);
    List<Resena> findByAprobada(boolean aprobada); // Para que el admin modere
}