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
        System.out.println(">>> Buscando usuario por email: " + email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        System.out.println(">>> userId: " + usuario.toString());

        Integer userId = usuario.getId();
        String rol;

        boolean esAdmin = administradorRepository.findByUsuario(usuario).isPresent();
        boolean esCliente = clienteRepository.findByUsuario(usuario).isPresent();
        System.out.println(">>> esAdmin: " + esAdmin);
        System.out.println(">>> esCliente: " + esCliente);

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
    // Nuevo método para obtener el objeto Usuario completo
    public Usuario getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
