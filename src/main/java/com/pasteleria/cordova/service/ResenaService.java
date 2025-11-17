package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.Cliente;
import com.pasteleria.cordova.model.Producto;
import com.pasteleria.cordova.model.Resena;
import com.pasteleria.cordova.repository.ResenaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;
    @Autowired
    private ClienteService clienteService; // Para obtener el cliente
    @Autowired
    private ProductoService productoService; // Para obtener el producto

    public Resena crearResena(Integer clienteId, Integer productoId, int calificacion, String comentario) {
        Cliente cliente = clienteService.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
        Producto producto = productoService.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        Resena resena = new Resena();
        resena.setCliente(cliente);
        resena.setProducto(producto);
        resena.setCalificacion(calificacion);
        resena.setComentario(comentario);
        resena.setFechaCreacion(LocalDateTime.now());
        resena.setAprobada(false); // Por defecto, las reseñas necesitan aprobación del admin
        return resenaRepository.save(resena);
    }

    public List<Resena> findByProducto(Integer productoId) {
        Producto producto = productoService.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
        return resenaRepository.findByProducto(producto);
    }

    public List<Resena> findApprovedReviewsByProducto(Integer productoId) {
        Producto producto = productoService.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
        return resenaRepository.findByProducto(producto).stream()
                .filter(Resena::isAprobada)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Resena> findAllPendingReviews() {
        return resenaRepository.findByAprobada(false);
    }

    public Optional<Resena> findById(Integer resenaId) {
        return resenaRepository.findById(resenaId);
    }

    // Aprobar o rechazar una reseña (para el administrador)
    public Resena setResenaAprobacion(Integer resenaId, boolean aprobada) {
        Resena resena = resenaRepository.findById(resenaId)
                .orElseThrow(() -> new IllegalArgumentException("Reseña no encontrada."));
        resena.setAprobada(aprobada);
        return resenaRepository.save(resena);
    }

    public void deleteResena(Integer resenaId) {
        resenaRepository.deleteById(resenaId);
    }
}