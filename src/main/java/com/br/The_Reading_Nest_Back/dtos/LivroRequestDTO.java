package com.br.The_Reading_Nest_Back.dtos;

import com.br.The_Reading_Nest_Back.entities.StatusLivro;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Explicação simples:
 * Formulário com as informações necessárias para cadastrar ou editar um livro no acervo da biblioteca:
 * qual o gênero, o título, quem escreveu, ano, código de barras (ISBN), estoque disponível e status.
 *
 * Explicação técnica:
 * DTO de entrada tipado e imutável que valida os dados fornecidos na requisição HTTP antes de alcançar
 * a camada de serviço. Aplica restrições de tamanho compatíveis com o DDL do MySQL e restrições numéricas
 * para impedir valores inconsistentes (como estoque negativo ou ano de publicação inválido).
 *
 * @param generoId Identificador do gênero ao qual o livro pertence.
 * @param titulo Título da obra literária.
 * @param autor Nome do autor ou autora.
 * @param descricao Sinopse descritiva da obra (opcional).
 * @param anoPublicacao Ano em que a obra foi publicada (mínimo 1000).
 * @param isbn Código internacional de 10 a 20 caracteres único (opcional).
 * @param quantidadeDisponivel Quantidade de exemplares livres para empréstimo (mínimo 0).
 * @param statusLivro Situação do livro ('ativo' ou 'desativado'). Se nulo no cadastro, assume 'ativo'.
 */
public record LivroRequestDTO(
        @NotBlank(message = "O ID DO GÊNERO É OBRIGATÓRIO")
        String generoId,

        @NotBlank(message = "O TÍTULO DO LIVRO É OBRIGATÓRIO")
        @Size(max = 150, message = "O TÍTULO DEVE TER NO MÁXIMO 150 CARACTERES")
        String titulo,

        @NotBlank(message = "O AUTOR É OBRIGATÓRIO")
        @Size(max = 100, message = "O AUTOR DEVE TER NO MÁXIMO 100 CARACTERES")
        String autor,

        String descricao,

        @Min(value = 1000, message = "O ANO DE PUBLICAÇÃO DEVE SER MAIOR OU IGUAL A 1000")
        Integer anoPublicacao,

        @Size(max = 20, message = "O ISBN DEVE TER NO MÁXIMO 20 CARACTERES")
        String isbn,

        @NotNull(message = "A QUANTIDADE DISPONÍVEL É OBRIGATÓRIA")
        @Min(value = 0, message = "A QUANTIDADE DISPONÍVEL NÃO PODE SER NEGATIVA")
        Integer quantidadeDisponivel,

        StatusLivro statusLivro
) {
}
