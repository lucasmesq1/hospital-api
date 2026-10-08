package br.com.portfolio.hospital.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticação HTTP Basic com perfis (papéis) de hospital.
 *
 * RECEPCAO: cadastra pacientes e interna.
 * MEDICO:   consulta prontuário e dá alta.
 * ADMIN:    tudo.
 *
 * Num sistema real, os usuários viriam de um provedor de identidade
 * (ex.: Keycloak / gov.br) em vez de ficarem em memória.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API sem sessão/cookies: CSRF não se aplica
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info",
                                 "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.POST, "/pacientes").hasAnyRole("RECEPCAO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/pacientes/*").hasAnyRole("RECEPCAO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/pacientes/**", "/internacoes/**", "/fhir/**")
                    .hasAnyRole("RECEPCAO", "MEDICO", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/internacoes").hasAnyRole("RECEPCAO", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/internacoes/*/alta").hasAnyRole("MEDICO", "ADMIN")
                .anyRequest().denyAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @Value("${app.users.recepcao-password}") String recepcaoPassword,
            @Value("${app.users.medico-password}") String medicoPassword,
            @Value("${app.users.admin-password}") String adminPassword) {

        return new InMemoryUserDetailsManager(
            User.withUsername("recepcao").password(encoder.encode(recepcaoPassword)).roles("RECEPCAO").build(),
            User.withUsername("medico").password(encoder.encode(medicoPassword)).roles("MEDICO").build(),
            User.withUsername("admin").password(encoder.encode(adminPassword)).roles("ADMIN").build());
    }
}
