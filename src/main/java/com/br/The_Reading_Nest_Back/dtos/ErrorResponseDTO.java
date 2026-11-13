package com.br.The_Reading_Nest_Back.dtos;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Explicação simples:
 * Formato padrão e único de retorno de erro que o backend envia quando alguma coisa dá errado
 * (ex: preencheu campo vazio, livro não encontrado ou sem estoque). Contém o código HTTP, a lista de erros e o horário.
 *
 * Explicação técnica:
 * DTO imutável de resposta a erros de API RESTful que padroniza o contrato de falhas exigido na spec.md.
 * Toda e qualquer exceção interceptada pelo ControllerAdvice é serializada com este payload exato,
 * facilitando o tratamento padronizado no frontend pelo leitor de respostas 'erro.erros'.
 *
 * @param status Código numérico do status HTTP retornado (ex: 400, 404, 409, 500).
 * @param erros Lista contendo as mensagens em letras maiúsculas descrevendo cada violação de regra.
 * @param horario Carimbo de data e hora em formato ISO do momento exato em que a falha foi registrada.
 */
public record ErrorResponseDTO(
        int status,
        List<String> erros,
        LocalDateTime horario
) {
}
