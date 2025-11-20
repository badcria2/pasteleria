package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.model.Resena;
import com.pasteleria.cordova.service.ResenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    // -----------------------------
    // CREAR RESEÑA
    // -----------------------------
    @PostMapping("/crear")
    public Resena crearResena(
            @RequestParam Integer clienteId,
            @RequestParam Integer productoId,
            @RequestParam Integer calificacion,
            @RequestParam(required = false) String comentario
    ) {
        return resenaService.crearResena(clienteId, productoId, calificacion, comentario);
    }

    // -----------------------------
    // LISTAR RESEÑAS POR PRODUCTO
    // -----------------------------
    @GetMapping("/producto/{productoId}")
    public List<Resena> getResenasByProducto(@PathVariable Integer productoId) {
        return resenaService.findByProducto(productoId);
    }

    // -----------------------------
    // LISTAR RESEÑAS APROBADAS POR PRODUCTO
    // -----------------------------
    @GetMapping("/producto/{productoId}/aprobadas")
    public List<Resena> getResenasAprobadas(@PathVariable Integer productoId) {
        return resenaService.findApprovedReviewsByProducto(productoId);
    }

    // -----------------------------
    // LISTAR TODAS LAS RESEÑAS PENDIENTES (MODERACIÓN)
    // -----------------------------
    @GetMapping("/pendientes")
    public List<Resena> getResenasPendientes() {
        return resenaService.findAllPendingReviews();
    }

    // -----------------------------
    // APROBAR O RECHAZAR RESEÑA
    // -----------------------------
    @PutMapping("/{resenaId}/aprobar")
    public Resena aprobarResena(
            @PathVariable Integer resenaId,
            @RequestParam boolean aprobada
    ) {
        return resenaService.setResenaAprobacion(resenaId, aprobada);
    }

    // -----------------------------
    // ELIMINAR RESEÑA
    // -----------------------------
    @DeleteMapping("/{resenaId}")
    public String eliminarResena(@PathVariable Integer resenaId) {
        resenaService.deleteResena(resenaId);
        return "Reseña eliminada correctamente.";
    }
}
