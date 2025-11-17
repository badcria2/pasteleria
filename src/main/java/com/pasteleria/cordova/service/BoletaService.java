package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.Boleta;
import com.pasteleria.cordova.model.Pedido;
import com.pasteleria.cordova.repository.BoletaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class BoletaService {

    @Autowired
    private BoletaRepository boletaRepository;
    // Otros servicios o configuraciones si necesitas calcular IGV, etc.

    private static final double IGV_RATE = 0.18; // Ejemplo: 18% de IGV

    @Transactional
    public Boleta generarBoletaParaPedido(Pedido pedido) {
        // Asegúrate de que el pedido ya tenga un total calculado
        if (pedido.getTotal() == null || pedido.getTotal() <= 0) {
            throw new IllegalArgumentException("El pedido no tiene un total válido para generar la boleta.");
        }

        // Si ya existe una boleta para este pedido, la devolvemos o actualizamos
        Optional<Boleta> existingBoleta = boletaRepository.findByPedido(pedido);
        if (existingBoleta.isPresent()) {
            return existingBoleta.get();
        }

        Boleta boleta = new Boleta();
        boleta.setPedido(pedido);
        boleta.setFechaEmision(LocalDate.now());

        double subtotal = pedido.getTotal() / (1 + IGV_RATE); // Asumiendo que el total del pedido ya incluye IGV
        double igv = pedido.getTotal() - subtotal;

        boleta.setSubtotal((float) subtotal);
        boleta.setIgv((float) igv);
        boleta.setTotal((float) pedido.getTotal());

        return boletaRepository.save(boleta);
    }

    public Optional<Boleta> findByPedido(Pedido pedido) {
        return boletaRepository.findByPedido(pedido);
    }

    public Optional<Boleta> findById(Integer boletaId) {
        return boletaRepository.findById(boletaId);
    }

    // Otros métodos de negocio para boletas
}