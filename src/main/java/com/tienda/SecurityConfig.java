package com.tienda;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    public static final String[] PUBLIC_URLS = {"/","/index","/consultas/**",
    "/fav/**","/webjars/**","/js/**","/login","/acceso_denegado"};
    public static final String[] USUARIO_URLS = {"/facturas/carrito"};
    public static final String[] VENDEDOR_URLS = {"/categoria/listado",
    "/producto/listado"};
    public static final String[] ADMIN_URLS = {"/categoria/**",
    "/producto/**","/usuario/**","/admin/**"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(USUARIO_URLS).hasRole("USUARIO")
                .requestMatchers(VENDEDOR_URLS).hasAnyRole("VENDEDOR","ADMIN")
                .requestMatchers(ADMIN_URLS).hasRole("ADMIN")
                .anyRequest().authenticated()
        );
        http.formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
        );
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        );
        http.exceptionHandling(ex -> ex.accessDeniedPage("/acceso_denegado"));
        http.sessionManagement(ses -> ses
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
        );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService user(PasswordEncoder passwordEncoder) {
        UserDetails user1 = User.builder().username("juan")
                .password(passwordEncoder.encode("123"))
                .roles("ADMIN")
                .build();
        UserDetails user2 = User.builder().username("rebeca")
                .password(passwordEncoder.encode("456"))
                .roles("VENDEDOR")
                .build();
        UserDetails user3 = User.builder().username("pedro")
                .password(passwordEncoder.encode("789"))
                .roles("USUARIO")
                .build();
        return new InMemoryUserDetailsManager(user1, user2, user3);
    }
}
