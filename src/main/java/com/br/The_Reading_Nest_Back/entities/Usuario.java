package com.br.The_Reading_Nest_Back.entities;

/**
 * Explicação simples:
 * Representa uma pessoa cadastrada na biblioteca (pode ser um leitor comum ou um administrador).
 * Guarda nome, e-mail para contato e login, senha segura, o perfil de acesso (admin ou cliente)
 * e a situação atual da conta.
 *
 * Explicação técnica:
 * Java record imutável correspondente à tabela 'usuarios' do MySQL.
 * A entidade é usada internamente e não é retornada pela API; as respostas públicas usam
 * UsuarioResponseDTO, que não possui o campo confidencial 'senha'.
 *
 * @param id Identificador único universal (UUID v4) do usuário.
 * @param nome Nome completo da pessoa cadastrada.
 * @param email Endereço de e-mail único utilizado para autenticação e comunicações.
 * @param senha Hash seguro via BCrypt, nunca incluído nos DTOs de resposta da API.
 * @param perfil Papel ou perfil de permissões de acesso do usuário ('admin' ou 'cliente').
 * @param statusConta Situação cadastral do usuário no sistema ('ativo', 'suspenso' ou 'desativado').
 */
public record Usuario(
        String id,
        String nome,
        String email,
        String senha,
        PerfilUsuario perfil,
        StatusConta statusConta
) {
}
