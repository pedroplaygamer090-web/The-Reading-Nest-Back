package com.br.The_Reading_Nest_Back.dtos;

import com.br.The_Reading_Nest_Back.entities.StatusAluguel;

import java.time.LocalDate;

/**
 * Explicação simples:
 * Dados usados pelo administrador para alterar o prazo de entrega ou a situação de um aluguel
 * (por exemplo, estender a data de devolução ou marcar manualmente como concluído).
 *
 * Explicação técnica:
 * DTO de entrada para a rota PUT /alugueis/{id}. Permite atualização parcial ou combinada
 * dos campos 'dataDevolucaoPrevista' e 'status', acionando as regras de devolução de estoque
 * caso haja transição para o estado 'concluido'.
 *
 * @param dataDevolucaoPrevista Nova data estipulada para entrega (pode ser nula se não for alterada).
 * @param status Novo status operacional do aluguel (pode ser nulo se não for alterado).
 */
public record AluguelUpdateDTO(
        LocalDate dataDevolucaoPrevista,
        StatusAluguel status
) {
}
