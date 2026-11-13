package com.br.The_Reading_Nest_Back.dtos;

import com.br.The_Reading_Nest_Back.entities.StatusConta;

/**
 * Explicação simples:
 * Informações públicas e seguras de um usuário retornadas para o frontend (como na tela Minha Conta
 * ou na listagem de usuários do administrador). Não contém a senha do usuário em hipótese alguma.
 *
 * Explicação técnica:
 * DTO imutável de saída que atende rigorosamente à regra de segurança de não exposição de credenciais.
 * Ao omitir o atributo 'senha', garante que nenhum hash criptográfico ou valor nulo relativo à senha
 * seja exposto nas respostas JSON da API RESTful.
 *
 * @param id Identificador UUID único universal do usuário.
 * @param nome Nome completo cadastrado no sistema.
 * @param email Endereço de e-mail do usuário.
 * @param statusConta Situação cadastral do usuário ('ativo', 'suspenso' ou 'desativado').
 */
public record UsuarioResponseDTO(
        String id,
        String nome,
        String email,
        StatusConta statusConta
) {
}
