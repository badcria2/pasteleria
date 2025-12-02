package com.pasteleria.cordova.config;

import com.pasteleria.cordova.security.SecurityUtils;
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
            // 🔐 Sanitizar datos antes de logging para prevenir CRLF injection
            String safePrincipal = SecurityUtils.sanitizeInput(principal != null ? principal.toString() : "null");
            logger.info("[AUTH EVENT] AuthenticationSuccess for principal type: {}", principal != null ? principal.getClass().getSimpleName() : "null");

            // If principal is a username (String), try to log additional info
            if (principal instanceof String) {
                String username = SecurityUtils.sanitizeInput((String) principal);
                if (SecurityUtils.isInputSecure(username)) {
                    usuarioRepository.findByEmail(username).ifPresent(u -> {
                        String safeEmail = SecurityUtils.sanitizeInput(u.getEmail());
                        logger.info("[AUTH EVENT] Usuario encontrado en DB. ID: {} Email length: {}", u.getId(), safeEmail.length());
                    });
                }
            }

        } else if (event instanceof AuthenticationFailureBadCredentialsEvent) {
            AuthenticationFailureBadCredentialsEvent ev = (AuthenticationFailureBadCredentialsEvent) event;
            Object principal = ev.getAuthentication().getPrincipal();
            logger.warn("[AUTH EVENT] AuthenticationFailureBadCredentials for principal type: {}", 
                       principal != null ? principal.getClass().getSimpleName() : "null");

            // Si es un string (email), comprobar si la contraseña enviada coincide con la guardada (solo para debug)
            try {
                if (principal instanceof String) {
                    String username = SecurityUtils.sanitizeInput((String) principal);
                    if (SecurityUtils.isInputSecure(username)) {
                        usuarioRepository.findByEmail(username).ifPresent(u -> {
                            // 🔐 NO loggear información sensible como contraseñas o hashes
                            logger.warn("[AUTH EVENT] Failed login attempt for existing user. UserID: {}", u.getId());
                        });
                    } else {
                        logger.warn("[AUTH EVENT] Failed login attempt with potentially malicious username");
                    }
                }
            } catch (Exception ex) {
                // 🔐 Usar mensaje seguro para errores
                String secureMessage = SecurityUtils.createSecureErrorMessage(ex.getMessage());
                logger.error("[AUTH EVENT] Error processing failure event: {}", secureMessage);
            }
        }
    }
}
