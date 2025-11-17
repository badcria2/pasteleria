package com.pasteleria.cordova.service;

import com.pasteleria.cordova.model.Usuario;
import com.pasteleria.cordova.repository.AdministradorRepository;
import com.pasteleria.cordova.repository.ClienteRepository;
import com.pasteleria.cordova.repository.UsuarioRepository;
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

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private AdministradorRepository administradorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Determinar roles del usuario
        List<GrantedAuthority> authorities;
        if (administradorRepository.findByUsuario(usuario).isPresent()) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (clienteRepository.findByUsuario(usuario).isPresent()) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        } else {
            // Usuario sin rol específico (podrías tener un rol "USER" por defecto o manejarlo como quieras)
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getContraseña(),
                authorities
        );
    }
}