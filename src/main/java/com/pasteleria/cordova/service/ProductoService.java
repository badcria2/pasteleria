package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.Producto;
import com.pasteleria.cordova.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> findAllProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> findById(Integer id) {
        return productoRepository.findById(id);
    }

    public Producto saveProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public void deleteProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    // Método para buscar productos por nombre/descripción (usando Spring Data JPA Query Methods)
    public List<Producto> searchProductos(String searchTerm) {
        return productoRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(searchTerm, searchTerm);
    }

    // Otros métodos de negocio para productos
}