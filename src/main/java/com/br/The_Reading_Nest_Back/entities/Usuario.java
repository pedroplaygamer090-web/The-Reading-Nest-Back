package com.br.The_Reading_Nest_Back.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Explicação simples:
 * Representa uma pessoa cadastrada na biblioteca (pode ser um leitor comum ou um administrador).
 * Guarda nome, e-mail para contato e login, senha segura e situação da conta.
 *
 * Explicação técnica:
 * Java record imutável correspondente à tabela 'usuarios' do MySQL.
 * Contém a anotação @JsonIgnore do pacote 'tools.jackson.annotation.JsonIgnore' (Jackson 3, padrão
 * do Spring Boot 4) no campo 'senha' para garantir que o hash confidencial nunca seja serializado
 * em eventuais respostas JSON, reforçando a segurança e impedindo vazamento estrutural de credenciais.
 *
 * @param id Identificador único universal (UUID v4) do usuário.
 * @param nome Nome completo da pessoa cadastrada.
 * @param email Endereço de e-mail único utilizado para autenticação e comunicações.
 * @param senha Hash seguro da senha criptografado via BCrypt (ignorado na serialização JSON).
 * @param statusConta Situação cadastral do usuário no sistema ('ativo', 'suspenso' ou 'desativado').
 */
public record Usuario(
        String id,
        String nome,
        String email,
        @JsonIgnore
        String senha,
        StatusConta statusConta
) {
}
