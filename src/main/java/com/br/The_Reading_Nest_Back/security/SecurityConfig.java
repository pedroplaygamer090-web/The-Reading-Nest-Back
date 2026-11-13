package com.br.The_Reading_Nest_Back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Explicação simples:
 * Configuração de segurança da biblioteca. Ela define que qualquer tela do frontend (como a página que
 * roda no Live Server) pode se comunicar com nossa API sem ser bloqueada, que a aplicação não precisa
 * guardar sessões pesadas na memória do servidor e fornece a ferramenta para transformar senhas em códigos seguros.
 *
 * Explicação técnica:
 * Classe de configuração central do Spring Security 7 anotada com @Configuration e @EnableWebSecurity.
 * Utiliza exclusivamente a sintaxe Lambda DSL, desabilita a proteção CSRF para operação REST pura,
 * define política de sessão como STATELESS, expõe a configuração do CORS via CorsConfigurationSource
 * para prevenir rejeição de requisições de preflight HTTP OPTIONS e libera temporariamente todos os endpoints
 * (.anyRequest().permitAll()) para integração imediata até a fase de autenticação por tokens.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define a cadeia de filtros de segurança (SecurityFilterChain) do Spring Security.
     *
     * @param http Construtor de segurança HTTP do Spring Security.
     * @return Cadeia de filtros configurada.
     * @throws Exception Caso ocorra erro de configuração interna.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    /**
     * Configura a política de CORS diretamente no Spring Security para permitir requisições cross-origin
     * originadas do frontend (Live Server nas portas 5500).
     *
     * @return Fonte de configuração CORS registrada para todas as rotas (/**).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://127.0.0.1:5500", "http://localhost:5500"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Expõe o codificador de senhas baseado no algoritmo BCrypt com salt aleatório.
     *
     * @return Instância de PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
