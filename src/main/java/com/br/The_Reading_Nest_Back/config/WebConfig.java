package com.br.The_Reading_Nest_Back.config;

import com.br.The_Reading_Nest_Back.entities.PerfilUsuario;
import com.br.The_Reading_Nest_Back.entities.StatusAluguel;
import com.br.The_Reading_Nest_Back.entities.StatusConta;
import com.br.The_Reading_Nest_Back.entities.StatusLivro;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Explicação simples:
 * Ajusta as configurações gerais de navegação da biblioteca: garante que qualquer busca retorne
 * por padrão 30 itens por página (com limite máximo de 100) e ensina o sistema a entender filtros
 * e valores tanto em maiúsculas quanto em minúsculas (por exemplo, aceitando tanto 'admin' quanto 'ADMIN').
 *
 * Explicação técnica:
 * Configuração MVC que customiza os resolvedores de argumentos e formatadores do Spring MVC.
 * Adiciona conversores case-insensitive para os enumeradores de domínio (StatusLivro, StatusConta, StatusAluguel e PerfilUsuario),
 * normalizando a entrada com trim().toLowerCase() antes de chamar o valueOf(), e registra o
 * PageableHandlerMethodArgumentResolver com página padrão de 30 itens e teto máximo de 100 itens.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Conversor case-insensitive para StatusLivro
        registry.addConverter(new Converter<String, StatusLivro>() {
            @Override
            public StatusLivro convert(String source) {
                if (source == null || source.isBlank()) {
                    return null;
                }
                return StatusLivro.valueOf(source.trim().toLowerCase());
            }
        });

        // Conversor case-insensitive para StatusConta
        registry.addConverter(new Converter<String, StatusConta>() {
            @Override
            public StatusConta convert(String source) {
                if (source == null || source.isBlank()) {
                    return null;
                }
                return StatusConta.valueOf(source.trim().toLowerCase());
            }
        });

        // Conversor case-insensitive para StatusAluguel
        registry.addConverter(new Converter<String, StatusAluguel>() {
            @Override
            public StatusAluguel convert(String source) {
                if (source == null || source.isBlank()) {
                    return null;
                }
                return StatusAluguel.valueOf(source.trim().toLowerCase());
            }
        });

        // Conversor case-insensitive para PerfilUsuario
        registry.addConverter(new Converter<String, PerfilUsuario>() {
            @Override
            public PerfilUsuario convert(String source) {
                if (source == null || source.isBlank()) {
                    return null;
                }
                return PerfilUsuario.valueOf(source.trim().toLowerCase());
            }
        });
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setFallbackPageable(PageRequest.of(0, 30));
        pageableResolver.setMaxPageSize(100);
        resolvers.add(pageableResolver);
    }
}
