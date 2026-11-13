package com.br.The_Reading_Nest_Back.entities;

/**
 * Explicação simples:
 * Define o nível de acesso e o papel de uma pessoa no sistema da biblioteca.
 * Pode ser um 'cliente' (leitor comum que consulta o acervo e aluga livros) ou
 * um 'admin' (funcionário ou administrador com permissão total para gerenciar livros, gêneros, usuários e aluguéis).
 *
 * Explicação técnica:
 * Enumeração em Java que mapeia diretamente a coluna ENUM('admin', 'cliente') da tabela 'usuarios' no MySQL.
 * Mantém os nomes rigorosamente em letras minúsculas sem acento, atendendo à convenção do projeto,
 * integrando-se nativamente com a autorização baseada em papéis (ROLE_ADMIN e ROLE_CLIENTE) do Spring Security.
 */
public enum PerfilUsuario {
    admin,
    cliente
}
