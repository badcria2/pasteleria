package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.Usuario;
import com.pasteleria.cordova.repository.AdministradorRepository;
import com.pasteleria.cordova.repository.ClienteRepository;
import com.pasteleria.cordova.repository.UsuarioRepository;
import com.pasteleria.cordova.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 🛡️ VALIDACIÓN DE SEGURIDAD: Verificar email por amenazas
        if (!SecurityUtils.isInputSecure(email)) {
            logger.warn("[SECURITY] Intento de login con email malicioso: {}", 
                       SecurityUtils.sanitizeInput(email));
            throw new UsernameNotFoundException("Formato de email no válido");
        }
        
        logger.debug("Autenticando usuario con email: {}", SecurityUtils.sanitizeInput(email));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("[AUTH] Intento de login fallido para email no registrado: {}", 
                               SecurityUtils.sanitizeInput(email));
                    return new UsernameNotFoundException("Credenciales incorrectas");
                });
        
        logger.debug("Usuario encontrado con ID: {}", usuario.getId());

        Integer userId = usuario.getId();
        String rol;

        boolean esAdmin = administradorRepository.findByUsuario(usuario).isPresent();
        boolean esCliente = clienteRepository.findByUsuario(usuario).isPresent();
        
        logger.debug("Roles del usuario ID {}: Admin={}, Cliente={}", userId, esAdmin, esCliente);

        if (esAdmin) {
            rol = "ROLE_ADMIN";
        } else if (esCliente) {
            rol = "ROLE_CLIENTE";
        } else {
            rol = "ROLE_USER";
        }

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(rol))
        );
    }
    // 🛡️ Método seguro para obtener el objeto Usuario completo
    public Usuario getUsuarioByEmail(String email) {
        // VALIDACIÓN DE SEGURIDAD: Verificar email por amenazas
        if (!SecurityUtils.isInputSecure(email)) {
            logger.warn("[SECURITY] Intento de búsqueda de usuario con email malicioso: {}", 
                       SecurityUtils.sanitizeInput(email));
            throw new UsernameNotFoundException("Formato de email no válido");
        }
        
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("[AUTH] Búsqueda fallida para email: {}", 
                               SecurityUtils.sanitizeInput(email));
                    return new UsernameNotFoundException("Usuario no encontrado");
                });
    }
}
