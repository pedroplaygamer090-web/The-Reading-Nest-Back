package com.br.The_Reading_Nest_Back.dtos;

/**
 * Explicação simples:
 * Permite ao administrador ou leitor informar uma justificativa ou motivo pelo qual
 * o aluguel foi cancelado antes do prazo previsto.
 *
 * Explicação técnica:
 * DTO opcional enviado no corpo da requisição POST /alugueis/{id}/cancelar.
 * Caso preenchido pelo administrador, a justificativa é registrada nos logs da aplicação
 * para fins de auditoria e controle de estoque da biblioteca.
 *
 * @param motivo Descrição textual da justificativa para o cancelamento (opcional).
 */
public record AluguelCancelamentoDTO(
        String motivo
) {
}
