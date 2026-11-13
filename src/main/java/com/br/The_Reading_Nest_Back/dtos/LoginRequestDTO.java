package com.br.The_Reading_Nest_Back.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Explicação simples:
 * Formulário preenchido na tela de login contendo o e-mail cadastrado e a senha do usuário
 * para entrar no sistema e receber o token de acesso.
 *
 * Explicação técnica:
 * DTO de entrada para autenticação em POST /auth/login.
 * Valida a presença obrigatória dos campos e a conformidade sintática do e-mail com a RFC 5322
 * antes do processamento das credenciais na camada de serviço.
 *
 * @param email Endereço de e-mail do usuário.
 * @param senha Senha em texto puro a ser validada contra o hash BCrypt.
 */
public record LoginRequestDTO(
        @NotBlank(message = "O E-MAIL É OBRIGATÓRIO")
        @Email(message = "FORMATO DE E-MAIL INVÁLIDO")
        String email,

        @NotBlank(message = "A SENHA É OBRIGATÓRIA")
        String senha
) {
}
