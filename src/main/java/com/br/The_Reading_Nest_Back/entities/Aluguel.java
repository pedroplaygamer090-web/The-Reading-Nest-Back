package com.br.The_Reading_Nest_Back.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Explicação simples:
 * Representa um empréstimo de livros feito por um leitor. Guarda quem alugou, o nome do leitor,
 * quando o aluguel foi realizado, qual o prazo limite para a devolução e se o empréstimo está ativo,
 * concluído ou atrasado.
 *
 * Explicação técnica:
 * Java record imutável que representa o registro principal da tabela 'alugueis' no MySQL
 * agregando o campo opcional 'usuarioNome' obtido via INNER JOIN com a tabela 'usuarios'.
 *
 * @param id Identificador único universal (UUID v4) do aluguel.
 * @param usuariosId Chave estrangeira que referencia o usuário titular do aluguel na tabela 'usuarios'.
 * @param usuarioNome Nome textual do usuário titular (carregado via junção relacional).
 * @param dataAluguel Momento exato em que a transação de aluguel foi registrada.
 * @param dataDevolucaoPrevista Data combinada para entrega e devolução física dos livros.
 * @param status Situação operacional do aluguel ('ativo', 'concluido' ou 'atrasado').
 */
public record Aluguel(
        String id,
        String usuariosId,
        String usuarioNome,
        LocalDateTime dataAluguel,
        LocalDate dataDevolucaoPrevista,
        StatusAluguel status
) {
    /**
     * Construtor de conveniência para criação da entidade sem junção de nome de usuário.
     */
    public Aluguel(
            String id,
            String usuariosId,
            LocalDateTime dataAluguel,
            LocalDate dataDevolucaoPrevista,
            StatusAluguel status
    ) {
        this(id, usuariosId, null, dataAluguel, dataDevolucaoPrevista, status);
    }
}
