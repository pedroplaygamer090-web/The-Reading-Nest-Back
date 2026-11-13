package com.br.The_Reading_Nest_Back.dtos;

import com.br.The_Reading_Nest_Back.entities.StatusLivro;

/**
 * Explicação simples:
 * Dados completos de um livro que o sistema envia para o frontend exibir na vitrine de livros,
 * no catálogo, na barra de pesquisa ou no modal de detalhes do livro.
 *
 * Explicação técnica:
 * DTO imutável de saída que já embute dados desnormalizados de relacionamento (como 'generoNome'),
 * obtidos por meio de INNER JOIN nas queries de leitura do JdbcTemplate. Dessa forma, o frontend
 * recebe em uma única requisição todos os dados visuais necessários sem exigir round-trips adicionais.
 *
 * @param id Identificador UUID único universal do livro.
 * @param generoId Identificador UUID do gênero vinculado.
 * @param generoNome Nome textual do gênero literário correspondente.
 * @param titulo Título completo da obra.
 * @param autor Nome do escritor ou escritora.
 * @param descricao Sinopse descritiva da obra.
 * @param anoPublicacao Ano em que a obra foi publicada.
 * @param isbn Código ISBN único da obra.
 * @param quantidadeDisponivel Número de exemplares livres para empréstimo.
 * @param statusLivro Situação do livro no catálogo ('ativo' ou 'desativado').
 */
public record LivroResponseDTO(
        String id,
        String generoId,
        String generoNome,
        String titulo,
        String autor,
        String descricao,
        Integer anoPublicacao,
        String isbn,
        Integer quantidadeDisponivel,
        StatusLivro statusLivro
) {
}
