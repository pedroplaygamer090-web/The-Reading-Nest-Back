package com.br.The_Reading_Nest_Back.dtos;

import com.br.The_Reading_Nest_Back.entities.StatusAluguel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Explicação simples:
 * Detalhes completos de um empréstimo de livros específico, trazendo não só o cabeçalho
 * do aluguel como também a lista completa de todos os livros que foram alugados com seus detalhes.
 *
 * Explicação técnica:
 * DTO composto de saída retornado na rota GET /alugueis/{id}.
 * Agrega a lista de 'LivroResponseDTO' associada ao aluguel por meio da consulta
 * na tabela 'detalhes_alugueis', permitindo ao frontend renderizar a visão detalhada do empréstimo.
 *
 * @param id Identificador UUID único universal do aluguel.
 * @param usuarioId Identificador UUID do usuário titular do aluguel.
 * @param usuarioNome Nome textual do usuário vinculado ao empréstimo.
 * @param dataAluguel Data e horário da efetivação do aluguel.
 * @param dataDevolucaoPrevista Data combinada para entrega dos livros.
 * @param status Situação atual do aluguel ('ativo', 'concluido' ou 'atrasado').
 * @param livros Coleção detalhada dos livros vinculados a este aluguel.
 */
public record AluguelDetalhadoResponseDTO(
        String id,
        String usuarioId,
        String usuarioNome,
        LocalDateTime dataAluguel,
        LocalDate dataDevolucaoPrevista,
        StatusAluguel status,
        List<LivroResponseDTO> livros
) {
}
