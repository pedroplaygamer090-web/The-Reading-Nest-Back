package com.br.The_Reading_Nest_Back.dtos;

import com.br.The_Reading_Nest_Back.entities.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Explicação simples:
 * Formulário preenchido por um usuário quando ele quer se cadastrar na biblioteca,
 * fornecendo seu nome, e-mail para acesso, uma senha com pelo menos 6 caracteres e,
 * opcionalmente (para administradores), o perfil de acesso desejado.
 *
 * Explicação técnica:
 * DTO de entrada para criação de novos usuários no sistema.
 * Utiliza o Jakarta Validation para validar formato padrão de e-mail RFC 5322 e comprimento mínimo
 * de 6 caracteres na senha antes que ela seja processada e transformada em hash BCrypt no Service.
 * O campo 'perfil' é opcional; quando omitido na requisição pública, o sistema assume 'cliente' por padrão.
 *
 * @param nome Nome completo do novo leitor ou administrador.
 * @param email Endereço de e-mail válido e único no sistema.
 * @param senha Senha em texto puro a ser criptografada de forma segura pelo backend.
 * @param perfil Perfil de acesso do usuário ('admin' ou 'cliente'). Opcional na requisição.
 */
public record UsuarioRequestDTO(
        @NotBlank(message = "O NOME DO USUÁRIO É OBRIGATÓRIO")
        @Size(max = 100, message = "O NOME DEVE TER NO MÁXIMO 100 CARACTERES")
        String nome,

        @NotBlank(message = "O E-MAIL É OBRIGATÓRIO")
        @Email(message = "FORMATO DE E-MAIL INVÁLIDO")
        @Size(max = 100, message = "O E-MAIL DEVE TER NO MÁXIMO 100 CARACTERES")
        String email,

        @NotBlank(message = "A SENHA É OBRIGATÓRIA")
        @Size(min = 6, message = "A SENHA DEVE TER NO MÍNIMO 6 CARACTERES")
        String senha,

        PerfilUsuario perfil
) {
}
