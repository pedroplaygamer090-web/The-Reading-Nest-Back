package com.br.The_Reading_Nest_Back.entities;

/**
 * Explicação simples:
 * Representa uma categoria ou gênero literário da biblioteca (por exemplo: Fantasia, Romance,
 * Ficção Científica, Tecnologia), contendo seu identificador e o nome da categoria.
 *
 * Explicação técnica:
 * Registro imutável (Java record) que modela a entidade da tabela 'generos' no MySQL.
 * Por ser um record nativo do Java, todos os atributos são 'final' e os métodos de acesso,
 * 'equals', 'hashCode' e 'toString' são gerados automaticamente pelo compilador sem necessidade
 * de bibliotecas externas ou código repetitivo.
 *
 * @param id Identificador único universal (UUID v4) da categoria literária no banco de dados.
 * @param nome Nome do gênero literário, com restrição de unicidade na tabela.
 */
public record Genero(
        String id,
        String nome
) {
}
