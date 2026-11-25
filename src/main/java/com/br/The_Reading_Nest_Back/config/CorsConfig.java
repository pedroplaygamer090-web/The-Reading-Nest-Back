package com.br.The_Reading_Nest_Back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Explicação simples:
 * Permite que a página do frontend aberta no navegador (seja localmente pelo Live Server
 * do VS Code ou hospedada online no GitHub Pages) consiga enviar requisições e conversar
 * com o nosso backend sem que o navegador bloqueie por regras de segurança de mesma origem.
 *
 * Explicação técnica:
 * Configuração MVC anotada com @Configuration implementando WebMvcConfigurer.
 * Registra o mapeamento global de CORS no servlet do Spring MVC utilizando allowedOriginPatterns("*")
 * para liberar requisições provenientes tanto do ambiente local quanto do domínio de produção
 * (GitHub Pages), autorizando os verbos HTTP comuns ('GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'HEAD')
 * com suporte a credenciais e cabeçalhos de autorização JWT ativados.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Define as regras globais de compartilhamento de recursos entre origens diferentes (CORS).
     *
     * @param registry Registro central do Spring MVC onde os padrões de rota e origens são configurados.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
