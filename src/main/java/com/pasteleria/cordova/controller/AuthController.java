package com.pasteleria.cordova.controller;


import com.pasteleria.cordova.dto.UsuarioRegistrationDto;
import com.pasteleria.cordova.model.Usuario;
import com.pasteleria.cordova.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registro")
    public String showRegistrationForm(Model model) {
        model.addAttribute("usuario", new UsuarioRegistrationDto());
        return "registro"; // Vista para registrar un nuevo usuario
    }

    @PostMapping("/registro")
    public String registerUserAccount(@ModelAttribute("usuario") @Validated UsuarioRegistrationDto registrationDto,
                                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "registro";
        }
        try {
            Usuario newUser = new Usuario(
                    registrationDto.getNombre(),
                    registrationDto.getEmail(),
                    registrationDto.getTelefono(),
                    registrationDto.getContraseña()
            );
            usuarioService.registrarUsuario(newUser);
            return "redirect:/registro?success"; // Redirigir con mensaje de éxito
        } catch (IllegalArgumentException e) {
            model.addAttribute("emailError", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Vista de inicio de sesión
    }

    // Spring Security maneja el POST de login
}