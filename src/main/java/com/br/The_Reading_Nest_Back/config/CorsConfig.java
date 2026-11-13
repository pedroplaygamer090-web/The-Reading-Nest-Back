package com.br.The_Reading_Nest_Back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Explicação simples:
 * Permite que a página do frontend aberta no navegador (por exemplo, através da extensão Live Server
 * do VS Code) consiga enviar requisições e conversar com o nosso backend sem que o navegador
 * bloqueie por regras de segurança de mesma origem.
 *
 * Explicação técnica:
 * Configuração MVC anotada com @Configuration implementando WebMvcConfigurer.
 * Registra o mapeamento global de CORS no servlet do Spring MVC para liberar requisições
 * provenientes das origens 'http://127.0.0.1:5500' e 'http://localhost:5500', autorizando os verbos
 * HTTP comuns ('GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'HEAD') com credenciais ativadas.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
