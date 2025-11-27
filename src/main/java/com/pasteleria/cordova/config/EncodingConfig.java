package com.pasteleria.cordova.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class EncodingConfig {
    // La configuración de codificación UTF-8 se maneja automáticamente 
    // a través de las propiedades en application.properties:
    // server.servlet.encoding.charset=UTF-8
    // server.servlet.encoding.enabled=true
    // server.servlet.encoding.force=true
}