package com.br.The_Reading_Nest_Back.entities;

/**
 * Explicação simples:
 * Representa um livro físico pertencente ao acervo da biblioteca, guardando dados como título,
 * autor, sinopse, ano, código ISBN, quantidade em estoque para empréstimo e se está ativo no catálogo.
 *
 * Explicação técnica:
 * Registro imutável (Java record) que mapeia diretamente as colunas da tabela 'livros' no MySQL.
 * Não utiliza anotações de ORM (como JPA/Hibernate), sendo hidratado manualmente por RowMappers
 * em conjunto com o Spring JdbcTemplate para garantir máxima performance e previsibilidade nas queries.
 *
 * @param id Identificador único universal (UUID v4) do livro.
 * @param generosId Chave estrangeira referenciando o id da tabela 'generos'.
 * @param titulo Nome do livro cadastrado no sistema.
 * @param autor Nome do escritor ou escritora da obra.
 * @param descricao Resumo ou sinopse da obra literária (pode ser nulo).
 * @param anoPublicacao Ano em que o livro foi publicado (pode ser nulo).
 * @param isbn Código internacional padronizado do livro (com constraint de unicidade no MySQL).
 * @param quantidadeDisponivel Número de exemplares atualmente físicos e livres para novos aluguéis.
 * @param statusLivro Situação do livro no sistema ('ativo' ou 'desativado' via soft-delete).
 */
public record Livro(
        String id,
        String generosId,
        String titulo,
        String autor,
        String descricao,
        Integer anoPublicacao,
        String isbn,
        Integer quantidadeDisponivel,
        StatusLivro statusLivro
) {
}
