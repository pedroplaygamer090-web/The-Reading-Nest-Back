package com.br.The_Reading_Nest_Back.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Explicação simples:
 * Representa um empréstimo de livros feito por um leitor. Guarda quem alugou, quando o aluguel
 * foi realizado, qual o prazo limite para a devolução e se o empréstimo está ativo, concluído ou atrasado.
 *
 * Explicação técnica:
 * Java record imutável que representa o registro principal (cabeçalho) da tabela 'alugueis' no MySQL.
 * Os tipos temporais utilizados são LocalDateTime (para data e hora do registro) e LocalDate
 * (para a data prevista de entrega), mapeados diretamente aos tipos DATETIME e DATE do banco.
 *
 * @param id Identificador único universal (UUID v4) do aluguel.
 * @param usuariosId Chave estrangeira que referencia o usuário titular do aluguel na tabela 'usuarios'.
 * @param dataAluguel Momento exato em que a transação de aluguel foi registrada.
 * @param dataDevolucaoPrevista Data combinada para entrega e devolução física dos livros.
 * @param status Situação operacional do aluguel ('ativo', 'concluido' ou 'atrasado').
 */
public record Aluguel(
        String id,
        String usuariosId,
        LocalDateTime dataAluguel,
        LocalDate dataDevolucaoPrevista,
        StatusAluguel status
) {
}
