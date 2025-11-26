package com.pasteleria.cordova.config;

import com.pasteleria.cordova.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public RequestCache requestCache() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        // No guardar requests de recursos estáticos
        requestCache.setRequestMatcher(new RequestMatcher() {
            @Override
            public boolean matches(HttpServletRequest request) {
                String uri = request.getRequestURI();
                // No guardar requests de imágenes, CSS, JS u otros recursos estáticos
                return !uri.startsWith(request.getContextPath() + "/css") &&
                       !uri.startsWith(request.getContextPath() + "/js") &&
                       !uri.startsWith(request.getContextPath() + "/imagenes") &&
                       !uri.startsWith(request.getContextPath() + "/uploads") &&
                       !uri.endsWith(".jpg") &&
                       !uri.endsWith(".png") &&
                       !uri.endsWith(".css") &&
                       !uri.endsWith(".js") &&
                       !uri.endsWith(".ico");
            }
        });
        return requestCache;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // Log authorities for debugging
            System.out.println("[AUTH SUCCESS] authorities=" + authentication.getAuthorities());

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

            boolean isCliente = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));

            System.out.println("[AUTH SUCCESS] isAdmin=" + isAdmin + ", isCliente=" + isCliente);

            // Verificar si hay una URL específica solicitada y si es válida
            String targetUrl = request.getParameter("targetUrl");
            System.out.println("[AUTH SUCCESS] targetUrl parameter=" + targetUrl);

            if (isAdmin) {
                System.out.println("[AUTH SUCCESS] Redirecting admin to dashboard");
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            }

            if (isCliente) {
                // Para clientes, verificar si hay una URL específica válida
                if (targetUrl != null && !targetUrl.isEmpty() && isValidClientUrl(targetUrl)) {
                    System.out.println("[AUTH SUCCESS] Redirecting cliente to targetUrl: " + targetUrl);
                    response.sendRedirect(request.getContextPath() + targetUrl);
                } else {
                    System.out.println("[AUTH SUCCESS] Redirecting cliente to home");
                    response.sendRedirect(request.getContextPath() + "/");
                }
                return;
            }

            // fallback default
            System.out.println("[AUTH SUCCESS] Fallback redirect to home");
            response.sendRedirect(request.getContextPath() + "/");
        };
    }

    private boolean isValidClientUrl(String url) {
        // Lista de URLs válidas para clientes después del login
        return url.startsWith("/") && 
               !url.startsWith("/admin") && 
               !url.startsWith("/imagenes") && 
               !url.startsWith("/css") && 
               !url.startsWith("/js") && 
               !url.startsWith("/uploads") && 
               !url.endsWith(".jpg") && 
               !url.endsWith(".png") && 
               !url.endsWith(".css") && 
               !url.endsWith(".js");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestCache().requestCache(requestCache())
                .and()
                .authorizeRequests()
                .antMatchers("/", "/index", "/index.html", "/registro", "/login",
                        "/css/**", "/js/**", "/images/**", "/uploads/**",
                        "/imagenes/**", "/estilos.css").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/cliente/**").hasRole("CLIENTE")
                .antMatchers("/carrito/**").hasRole("CLIENTE") // Asegura que solo clientes puedan usar el carrito
                .antMatchers("/pedidos/**").hasRole("CLIENTE") // Asegura que solo clientes puedan ver y crear pedidos
                .anyRequest().authenticated()
                .and()

                .formLogin()
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(customAuthenticationSuccessHandler())
                    .failureUrl("/login?error=true")
                    .permitAll()
                .and()

                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll();
    }
}
