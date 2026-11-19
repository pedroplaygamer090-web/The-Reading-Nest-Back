package com.br.The_Reading_Nest_Back.entities;

import java.util.List;

/**
 * Explicação simples:
 * Representa a visão completa de um aluguel de livros, contendo as informações do aluguel em si
 * e a lista com todos os livros que o leitor retirou.
 *
 * Explicação técnica:
 * Objeto de domínio composto (agregado) que encapsula a entidade cabeçalho 'Aluguel'
 * e a coleção de entidades 'Livro' associadas através da tabela relacional 'detalhes_alugueis'.
 *
 * @param aluguel Entidade principal com dados cadastrais e temporais do empréstimo.
 * @param livros Lista das entidades de livros vinculadas a este empréstimo.
 */
public record AluguelCompleto(
        Aluguel aluguel,
        List<Livro> livros
) {
}
