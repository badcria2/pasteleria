package com.pasteleria.cordova;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "123456"; // Por ejemplo: "password123"
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Contraseña encriptada para '" + rawPassword + "': " + encodedPassword);

        // Puedes verificar si una contraseña coincide
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("¿Coinciden? " + matches);

        // Para verificar con la que tienes en la DB
        String dbPassword = "$2a$10$vt2PoCcS4ga0r1g/Vo3Rf./9xDwV3RKYjG5f76CM258W4J/gcu78W"; // La de tu log
        String inputPassword = "123456"; // La que usas para loguearte
        boolean dbMatches = passwordEncoder.matches(inputPassword, dbPassword);
        System.out.println("¿Coincide la contraseña de entrada con la de la DB? " + dbMatches);
    }
}