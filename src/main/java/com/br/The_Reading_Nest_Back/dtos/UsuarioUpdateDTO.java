package com.br.The_Reading_Nest_Back.dtos;

import com.br.The_Reading_Nest_Back.entities.StatusConta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Explicação simples:
 * Formulário utilizado para atualizar dados cadastrais de um usuário. Permite alterar o nome,
 * e-mail, status da conta e, opcionalmente, redefinir a senha de acesso (se enviada).
 *
 * Explicação técnica:
 * DTO de entrada para operações de atualização parcial ou total via PUT /usuarios/{id}.
 * A validação @Size(min = 6) na senha só é acionada caso o campo não seja nulo, permitindo
 * atualizar outros dados do usuário sem a obrigatoriedade de reescrever ou reenviar a senha atual.
 *
 * @param nome Novo nome completo do usuário.
 * @param email Novo endereço de e-mail do usuário.
 * @param senha Nova senha opcional (se preenchida, deve possuir no mínimo 6 caracteres).
 * @param statusConta Situação da conta do usuário ('ativo', 'suspenso' ou 'desativado').
 */
public record UsuarioUpdateDTO(
        @NotBlank(message = "O NOME DO USUÁRIO É OBRIGATÓRIO")
        @Size(max = 100, message = "O NOME DEVE TER NO MÁXIMO 100 CARACTERES")
        String nome,

        @NotBlank(message = "O E-MAIL É OBRIGATÓRIO")
        @Email(message = "FORMATO DE E-MAIL INVÁLIDO")
        @Size(max = 100, message = "O E-MAIL DEVE TER NO MÁXIMO 100 CARACTERES")
        String email,

        @Size(min = 6, message = "A SENHA DEVE TER NO MÍNIMO 6 CARACTERES")
        String senha,

        StatusConta statusConta
) {
}
