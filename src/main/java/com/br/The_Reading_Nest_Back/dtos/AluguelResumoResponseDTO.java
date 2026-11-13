package com.br.The_Reading_Nest_Back.dtos;

import com.br.The_Reading_Nest_Back.entities.StatusAluguel;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Explicação simples:
 * Resumo enxuto de um empréstimo de livros, exibindo quem alugou, quando alugou, prazo e status.
 * É usado nas listagens gerais do administrador e no histórico do usuário para ser muito rápido de carregar.
 *
 * Explicação técnica:
 * DTO de saída otimizado para consultas paginadas de alta performance.
 * Não carrega a relação filha de livros (evita o problema de performance N+1 e grandes payloads),
 * trazendo apenas os dados do cabeçalho unidos ao nome do usuário via INNER JOIN.
 *
 * @param id Identificador UUID único universal do aluguel.
 * @param usuarioId Identificador UUID do usuário titular do aluguel.
 * @param usuarioNome Nome do usuário vinculado ao aluguel.
 * @param dataAluguel Data e horário do momento em que o aluguel foi realizado.
 * @param dataDevolucaoPrevista Data combinada para devolução dos livros.
 * @param status Situação operacional do aluguel ('ativo', 'concluido' ou 'atrasado').
 */
public record AluguelResumoResponseDTO(
        String id,
        String usuarioId,
        String usuarioNome,
        LocalDateTime dataAluguel,
        LocalDate dataDevolucaoPrevista,
        StatusAluguel status
) {
}
