package com.br.The_Reading_Nest_Back.entities;

/**
 * Explicação simples:
 * Representa os possíveis estados de um livro dentro do sistema da biblioteca:
 * se ele está ativo (disponível para consulta e aluguel) ou desativado (soft-delete,
 * não visível para novos aluguéis).
 *
 * Explicação técnica:
 * Enumeração em Java mapeada diretamente para a coluna ENUM('ativo', 'desativado') da tabela
 * 'livros' no MySQL. O valor é mantido em letras minúsculas sem acento para garantir consistência
 * estrita com o banco de dados e evitar problemas de collation/encoding.
 */
public enum StatusLivro {
    ativo,
    desativado
}
