package com.br.The_Reading_Nest_Back.entities;

/**
 * Explicação simples:
 * Liga cada livro emprestado ao seu respectivo aluguel. Como um único aluguel pode conter vários
 * livros diferentes, essa classe faz a ponte entre o empréstimo e os livros levados pelo leitor.
 *
 * Explicação técnica:
 * Java record imutável que representa a tabela associativa n-para-n 'detalhes_alugueis' no MySQL.
 * Possui sua própria chave primária em formato UUID v4 e chaves estrangeiras vinculando
 * 'alugueis_id' e 'livros_id', viabilizando a relação de um aluguel para múltiplos livros.
 *
 * @param id Identificador único universal (UUID v4) do item vinculado ao aluguel.
 * @param alugueisId Chave estrangeira que referencia a tabela 'alugueis'.
 * @param livrosId Chave estrangeira que referencia a tabela 'livros'.
 */
public record DetalheAluguel(
        String id,
        String alugueisId,
        String livrosId
) {
}
