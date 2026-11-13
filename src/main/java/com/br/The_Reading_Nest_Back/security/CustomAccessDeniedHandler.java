package com.br.The_Reading_Nest_Back.security;

import com.br.The_Reading_Nest_Back.dtos.ErrorResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Explicação simples:
 * Cuida das situações em que uma pessoa até está logada no sistema (tem crachá), mas tenta fazer
 * algo para o qual não tem permissão (por exemplo, um leitor tentando excluir livros ou ver os dados de outro usuário).
 * Devolve uma resposta em formato JSON avisando que o acesso é proibido (código 403).
 *
 * Explicação técnica:
 * Manipulador de acesso negado que implementa AccessDeniedHandler do Spring Security.
 * Intercepta AccessDeniedException geradas quando um usuário autenticado tenta consumir endpoints protegidos
 * por autoridade superior (ex: rota hasRole('ADMIN') acessada por usuário com ROLE_CLIENTE), serializando
 * o 'ErrorResponseDTO' com HTTP 403 Forbidden via Jackson 3 (tools.jackson.databind.ObjectMapper).
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponseDTO erro = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                List.of("ACESSO NEGADO: VOCÊ NÃO TEM PERMISSÃO PARA ACESSAR ESTE RECURSO"),
                LocalDateTime.now()
        );

        response.getWriter().write(objectMapper.writeValueAsString(erro));
    }
}
