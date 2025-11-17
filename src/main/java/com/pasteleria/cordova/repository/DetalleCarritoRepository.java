package com.pasteleria.cordova.repository;

import com.pasteleria.cordova.model.Carrito;
import com.pasteleria.cordova.model.DetalleCarrito;
import com.pasteleria.cordova.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Integer> {
    List<DetalleCarrito> findByCarrito(Carrito carrito);
    Optional<DetalleCarrito> findByCarritoAndProducto(Carrito carrito, Producto producto);
    void deleteByCarrito(Carrito carrito); // Para vaciar el carrito
}