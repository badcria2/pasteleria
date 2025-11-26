package com.pasteleria.cordova.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.pasteleria.cordova.model.Pedido;
import com.pasteleria.cordova.service.PedidoService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private PedidoService pedidoService;
    
    @GetMapping("/pedidos/{id}/details")
    @ResponseBody
    public ResponseEntity<Pedido> getPedidoDetails(@PathVariable Integer id) {
        try {
            return pedidoService.getPedidoById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
