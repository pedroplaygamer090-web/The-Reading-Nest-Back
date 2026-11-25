package com.br.The_Reading_Nest_Back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Explicação simples:
 * A central de segurança da biblioteca. Define as regras de quem pode acessar cada área do sistema:
 * permite que qualquer pessoa veja os livros, cadastre-se ou faça login, mas exige crachá de administrador
 * para cadastrar novos livros, criar categorias ou gerenciar outros usuários. Também integra a checagem do token JWT.
 *
 * Explicação técnica:
 * Configuração de segurança declarativa do Spring Security 7.
 * Opera em regime STATELESS com proteção CSRF desabilitada (adequada para APIs REST pura com tokens Bearer),
 * registra o JwtAuthenticationFilter antes do UsernamePasswordAuthenticationFilter padrão, mapeia os handlers
 * de erro CustomAuthenticationEntryPoint (401) e CustomAccessDeniedHandler (403), e aplica regras granulares
 * de autorização HTTP combinando verbos, rotas e papéis (ROLE_ADMIN e ROLE_CLIENTE).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    /**
     * Define a cadeia de filtros de segurança (SecurityFilterChain) com regras estritas de autorização.
     *
     * @param http Construtor de segurança HTTP do Spring Security.
     * @return Cadeia de filtros configurada.
     * @throws Exception Caso ocorra erro de configuração.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Requisições prévias de CORS (Preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. Rotas Públicas (acessíveis sem token)
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/generos", "/generos/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/livros", "/livros/*").permitAll()

                        // 3. Rotas Exclusivas de Administrador (hasRole("ADMIN"))
                        .requestMatchers(HttpMethod.POST, "/generos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/generos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/generos/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/livros").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/livros/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/livros/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/alugueis").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/alugueis/*").hasRole("ADMIN")

                        // 4. Rotas Autenticadas (Cliente e Admin — com validação de posse/dono nos serviços)
                        .requestMatchers(HttpMethod.GET, "/usuarios/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/usuarios/*/alugueis").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/usuarios/*").authenticated()

                        .requestMatchers(HttpMethod.POST, "/alugueis").authenticated()
                        .requestMatchers(HttpMethod.GET, "/alugueis/meus").authenticated()
                        .requestMatchers(HttpMethod.GET, "/alugueis/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/alugueis/*/devolver").authenticated()
                        .requestMatchers(HttpMethod.POST, "/alugueis/*/cancelar").authenticated()

                        // 5. Qualquer outra rota não mapeada explicitamente exige autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura a política de CORS para o frontend (Live Server nas portas 5500).
     *
     * @return Fonte de configuração CORS registrada para todas as rotas (/**).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
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
