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
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registro")
    public String showRegistrationForm(Model model) {
        model.addAttribute("usuario", new UsuarioRegistrationDto());
        return "registro";
    }

    @PostMapping("/registro")
    public String registerUserAccount(
            @ModelAttribute("usuario") @Validated UsuarioRegistrationDto registrationDto,
            BindingResult result,
            Model model) {

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
            return "redirect:/registro?success";

        } catch (IllegalArgumentException e) {
            model.addAttribute("emailError", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("loginError", "Credenciales incorrectas");
        }
        return "login";
    }

    // No hay POST /login → Spring Security ya lo maneja
}
