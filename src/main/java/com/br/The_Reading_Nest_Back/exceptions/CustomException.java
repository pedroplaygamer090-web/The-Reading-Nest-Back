package com.br.The_Reading_Nest_Back.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

/**
 * Explicação simples:
 * Exceção personalizada da biblioteca utilizada para interromper fluxos incorretos
 * (como tentar alugar um livro esgotado ou buscar um gênero inexistente) e carregar
 * a mensagem correta que deve ser exibida ao usuário final.
 *
 * Explicação técnica:
 * Subclasse de RuntimeException desacoplada que armazena explicitamente o status HTTP correspondente
 * e uma lista imutável de mensagens de erro. Possui construtor de conveniência para mensagem singular,
 * permitindo lançamento limpo na camada de Service sem depender de estruturas complexas do Spring MVC.
 */
public class CustomException extends RuntimeException {

    private final HttpStatus status;
    private final List<String> erros;

    /**
     * Construtor completo com status HTTP e coleção de erros.
     *
     * @param status Código de status HTTP representativo da regra violada.
     * @param erros Lista contendo as mensagens descritivas dos erros ocorridos.
     */
    public CustomException(HttpStatus status, List<String> erros) {
        super(erros != null && !erros.isEmpty() ? erros.getFirst() : status.getReasonPhrase());
        this.status = status;
        this.erros = erros != null ? List.copyOf(erros) : Collections.emptyList();
    }

    /**
     * Construtor de conveniência para lançamento com mensagem única.
     *
     * @param status Código de status HTTP.
     * @param mensagem Mensagem única de erro que será encapsulada em uma lista imutável.
     */
    public CustomException(HttpStatus status, String mensagem) {
        this(status, List.of(mensagem));
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getErros() {
        return erros;
    }
}
