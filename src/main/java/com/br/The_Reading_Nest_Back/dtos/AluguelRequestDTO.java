package com.br.The_Reading_Nest_Back.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Explicação simples:
 * Dados enviados pelo leitor quando ele confirma o empréstimo de um ou mais livros no carrinho
 * ou modal de aluguel: quem está alugando, a data prevista em que ele vai devolver e a lista de livros escolhidos.
 *
 * Explicação técnica:
 * DTO de entrada para a criação de um aluguel (POST /alugueis).
 * A anotação @Future assegura que a data de devolução seja estritamente posterior à data corrente,
 * e @NotEmpty garante que ao menos um identificador de livro seja enviado no array 'livrosIds'.
 *
 * @param usuarioId Identificador UUID do leitor que está realizando o empréstimo.
 * @param dataDevolucaoPrevista Data combinada no futuro para retorno dos exemplares físicos.
 * @param livrosIds Lista contendo um ou mais identificadores UUID dos livros alugados.
 */
public record AluguelRequestDTO(
        @NotBlank(message = "O ID DO USUÁRIO É OBRIGATÓRIO")
        String usuarioId,

        @NotNull(message = "A DATA DE DEVOLUÇÃO PREVISTA É OBRIGATÓRIA")
        @Future(message = "A DATA DE DEVOLUÇÃO PREVISTA DEVE SER UMA DATA FUTURA")
        LocalDate dataDevolucaoPrevista,

        @NotEmpty(message = "A LISTA DE LIVROS NÃO PODE ESTAR VAZIA")
        List<String> livrosIds
) {
}
