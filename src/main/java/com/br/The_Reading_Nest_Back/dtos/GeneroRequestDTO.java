package com.br.The_Reading_Nest_Back.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Explicação simples:
 * Dados enviados pelo cliente (ou administrador) quando se deseja cadastrar ou alterar
 * o nome de uma categoria ou gênero literário na biblioteca.
 *
 * Explicação técnica:
 * Objeto de Transferência de Dados (DTO) imutável implementado como Java record.
 * Utiliza anotações do Jakarta Bean Validation (jakarta.validation.constraints) para validar
 * se o nome não está em branco e não ultrapassa o limite físico de 60 caracteres da coluna no MySQL.
 *
 * @param nome Nome do gênero literário a ser cadastrado ou atualizado.
 */
public record GeneroRequestDTO(
        @NotBlank(message = "O NOME DO GÊNERO É OBRIGATÓRIO")
        @Size(max = 60, message = "O NOME DEVE TER NO MÁXIMO 60 CARACTERES")
        String nome
) {
}
