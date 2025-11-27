package com.pasteleria.cordova.controller;

import com.pasteleria.cordova.dto.UsuarioRegistrationDto;
import com.pasteleria.cordova.model.Usuario;
import com.pasteleria.cordova.model.Cliente;
import com.pasteleria.cordova.service.UsuarioService;
import com.pasteleria.cordova.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;


@Controller
public class AuthController {

    private static final String REGISTRO_VIEW = "cliente/registro";
    private static final String LOGIN_REDIRECT = "redirect:/login";
    
    private final UsuarioService usuarioService;
    private final ClienteService clienteService;
    
    public AuthController(UsuarioService usuarioService, ClienteService clienteService) {
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
    }

    @GetMapping("/registro")
    public String showRegistrationForm(Model model) {
        model.addAttribute("usuario", new UsuarioRegistrationDto());
        return REGISTRO_VIEW;
    }

    @PostMapping("/registro")
    public String registerUserAccount(
            @ModelAttribute("usuario") @Valid UsuarioRegistrationDto registrationDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validar que las contraseñas coincidan
        if (!registrationDto.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "error.passwordMismatch", "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return REGISTRO_VIEW;
        }

        try {
            // Crear el usuario
            Usuario newUser = new Usuario(
                    registrationDto.getNombre(),
                    registrationDto.getEmail(),
                    registrationDto.getTelefono(),
                    registrationDto.getPassword()
            );

            Usuario savedUser = usuarioService.registrarUsuario(newUser);
            
            // Crear el cliente asociado al usuario
            Cliente cliente = new Cliente();
            cliente.setUsuario(savedUser);
            cliente.setDireccion(registrationDto.getDireccion());
            clienteService.updateCliente(cliente);

            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Registro exitoso! Ya puedes iniciar sesión con tu cuenta.");
            return LOGIN_REDIRECT;

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return REGISTRO_VIEW;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error interno del servidor. Por favor, intenta nuevamente.");
            return REGISTRO_VIEW;
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
