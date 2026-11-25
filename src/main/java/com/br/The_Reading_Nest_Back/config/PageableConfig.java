package com.br.The_Reading_Nest_Back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
@EnableSpringDataWebSupport
public class PageableConfig {

    @Bean
    PageableHandlerMethodArgumentResolverCustomizer pageableResolverCustomizer(
            @Value("${spring.data.web.pageable.default-page-size:30}") int tamanhoPadrao,
            @Value("${spring.data.web.pageable.max-page-size:100}") int tamanhoMaximo
    ) {
        return resolver -> {
            resolver.setFallbackPageable(PageRequest.of(0, tamanhoPadrao));
            resolver.setMaxPageSize(tamanhoMaximo);
        };
    }
}
