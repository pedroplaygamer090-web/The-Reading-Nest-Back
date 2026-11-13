package com.br.The_Reading_Nest_Back.dtos;

/**
 * Explicação simples:
 * Resposta devolvida pela biblioteca quando alguém consulta uma categoria ou após cadastrar
 * um gênero literário com sucesso, trazendo o código do gênero e seu nome.
 *
 * Explicação técnica:
 * DTO imutável de saída serializado para JSON pelo Spring MVC.
 * Isola o contrato de resposta da entidade interna 'Genero', permitindo evolução independente
 * da API sem quebrar compatibilidade com as páginas do frontend.
 *
 * @param id Identificador UUID único universal do gênero literário.
 * @param nome Nome do gênero literário retornado pelo banco de dados.
 */
public record GeneroResponseDTO(
        String id,
        String nome
) {
}
