package com.pasteleria.cordova.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
public class CheckoutRequestDTO {
    private String metodoPago;
    private String direccionEnvio;
    private BigDecimal costoEnvio;


}