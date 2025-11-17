package com.pasteleria.cordova.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "boleta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Boleta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedidoId", referencedColumnName = "id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(name = "fechaEmision", nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private Float subtotal;

    @Column(nullable = false)
    private Float igv;

    @Column(nullable = false)
    private Float total;
}