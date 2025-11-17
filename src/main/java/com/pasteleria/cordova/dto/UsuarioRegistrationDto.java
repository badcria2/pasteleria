package com.pasteleria.cordova.dto;


import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UsuarioRegistrationDto {
   // @NotEmpty(message = "El nombre no puede estar vacío")
    private String nombre;

   // @NotEmpty(message = "El email no puede estar vacío")
   // @Email(message = "Email inválido")
    private String email;

    private String telefono;

   // @NotEmpty(message = "La contraseña no puede estar vacía")
    private String contraseña;

    //@NotEmpty(message = "Confirmar contraseña no puede estar vacío")
    private String confirmarContraseña; // Para validación de que coincidan

    // Puedes agregar validación personalizada para que las contraseñas coincidan
}