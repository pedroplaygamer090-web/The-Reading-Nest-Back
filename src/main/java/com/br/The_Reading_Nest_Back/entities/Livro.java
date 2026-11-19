package com.br.The_Reading_Nest_Back.entities;

/**
 * Explicação simples:
 * Representa um livro físico pertencente ao acervo da biblioteca, guardando dados como título,
 * autor, sinopse, ano, código ISBN, quantidade em estoque para empréstimo, a situação no catálogo
 * e o nome da categoria literária vinculada.
 *
 * Explicação técnica:
 * Registro imutável (Java record) que mapeia a tabela 'livros' no MySQL e transporta a projeção
 * opcional 'generoNome' obtida através de INNER JOIN com a tabela 'generos'. Possui construtor de
 * conveniência para criação a partir de dados brutos da tabela sem junção.
 *
 * @param id Identificador único universal (UUID v4) do livro.
 * @param generosId Chave estrangeira referenciando o id da tabela 'generos'.
 * @param generoNome Nome textual da categoria literária vinculada (opcional/carregado via join).
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
        String generoNome,
        String titulo,
        String autor,
        String descricao,
        Integer anoPublicacao,
        String isbn,
        Integer quantidadeDisponivel,
        StatusLivro statusLivro
) {
    /**
     * Construtor de conveniência sem o nome textual do gênero (definindo-o como nulo).
     */
    public Livro(
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
        this(id, generosId, null, titulo, autor, descricao, anoPublicacao, isbn, quantidadeDisponivel, statusLivro);
    }
}
