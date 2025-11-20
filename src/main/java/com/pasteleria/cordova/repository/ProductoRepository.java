package com.pasteleria.cordova.repository;


import com.pasteleria.cordova.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // Método para buscar productos por nombre o descripción, ignorando mayúsculas/minúsculas
    List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String nombre, String descripcion);

    List<Producto> findByCategoria(String categoria);

}