package com.br.The_Reading_Nest_Back.entities;

/**
 * Explicação simples:
 * Define a situação atual de um aluguel de livros: ativo (livros estão com o leitor dentro do prazo),
 * concluido (livros já foram devolvidos à biblioteca) ou atrasado (passou da data combinada para entrega).
 *
 * Explicação técnica:
 * Enumeração em Java alinhada aos valores da coluna ENUM('ativo', 'concluido', 'atrasado')
 * da tabela 'alugueis' no MySQL. O uso do valor sem acento 'concluido' previne inconsistências
 * em consultas SQL sob collations case/accent-insensitive ou ambientes com codificação divergente.
 */
public enum StatusAluguel {
    ativo,
    concluido,
    atrasado
}
