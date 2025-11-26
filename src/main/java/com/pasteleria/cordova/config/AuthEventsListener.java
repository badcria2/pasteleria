package com.pasteleria.cordova.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.pasteleria.cordova.repository.UsuarioRepository;
import com.pasteleria.cordova.model.Usuario;

@Component
public class AuthEventsListener implements ApplicationListener<ApplicationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AuthEventsListener.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof AuthenticationSuccessEvent) {
            AuthenticationSuccessEvent ev = (AuthenticationSuccessEvent) event;
            Object principal = ev.getAuthentication().getPrincipal();
            logger.info("[AUTH EVENT] AuthenticationSuccess for principal={}", principal);
            logger.info("[AUTH EVENT] authorities={}", ev.getAuthentication().getAuthorities());

            // If principal is a username (String), try to log additional info
            if (principal instanceof String) {
                String username = (String) principal;
                usuarioRepository.findByEmail(username).ifPresent(u -> {
                    logger.info("[AUTH EVENT] Usuario(id={}, email={}) encontrado en DB. passwordHashLength={}", u.getId(), u.getEmail(), u.getPassword() != null ? u.getPassword().length() : 0);
                });
            }

        } else if (event instanceof AuthenticationFailureBadCredentialsEvent) {
            AuthenticationFailureBadCredentialsEvent ev = (AuthenticationFailureBadCredentialsEvent) event;
            Object principal = ev.getAuthentication().getPrincipal();
            logger.warn("[AUTH EVENT] AuthenticationFailureBadCredentials for principal={}", principal);

            // Si es un string (email), comprobar si la contraseña enviada coincide con la guardada (solo para debug)
            try {
                if (principal instanceof String) {
                    String username = (String) principal;
                    Object creds = ev.getAuthentication().getCredentials();
                    String attempted = creds != null ? creds.toString() : null;
                    usuarioRepository.findByEmail(username).ifPresent(u -> {
                        String storedHash = u.getPassword();
                        boolean matches = false;
                        if (attempted != null && storedHash != null) {
                            try {
                                matches = passwordEncoder.matches(attempted, storedHash);
                            } catch (Exception ex) {
                                logger.warn("[AUTH EVENT] Error al comprobar passwordEncoder.matches: {}", ex.getMessage());
                            }
                        }
                        logger.warn("[AUTH EVENT] Login attempt for user='{}'. storedHashLength={} matchesSubmittedPassword={}", username, storedHash != null ? storedHash.length() : 0, matches);
                    });
                }
            } catch (Exception ex) {
                logger.error("[AUTH EVENT] Error processing failure event: {}", ex.getMessage(), ex);
            }
        }
    }
}
