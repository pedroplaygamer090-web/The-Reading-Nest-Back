package com.br.The_Reading_Nest_Back.security;

import com.br.The_Reading_Nest_Back.dtos.ErrorResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Explicação simples:
 * Cuida das situações em que uma pessoa tenta acessar uma área restrita da biblioteca sem apresentar
 * um crachá (token JWT) válido ou quando o crachá já venceu. Devolve uma resposta bonita em formato JSON
 * avisando que o acesso não foi autorizado (código 401).
 *
 * Explicação técnica:
 * Ponto de entrada de autenticação que implementa AuthenticationEntryPoint do Spring Security.
 * Captura falhas de autenticação disparadas durante o ciclo de filtros HTTP (ex: ausência de token, assinatura
 * inválida ou token expirado) e serializa o DTO padronizado ErrorResponseDTO com HTTP 401 Unauthorized
 * diretamente no corpo da resposta via Jackson 3 (tools.jackson.databind.ObjectMapper).
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponseDTO erro = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                List.of("ACESSO NÃO AUTORIZADO: TOKEN AUSENTE, INVÁLIDO OU EXPIRADO"),
                LocalDateTime.now()
        );

        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }
}
