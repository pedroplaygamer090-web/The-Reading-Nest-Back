package com.br.The_Reading_Nest_Back.entities;

/**
 * Explicação simples:
 * Representa a situação cadastral da conta de um usuário na biblioteca. Pode estar ativa
 * (pode navegar e alugar), suspensa (temporariamente impedida de alugar) ou desativada (desligada do sistema).
 *
 * Explicação técnica:
 * Enumeração em Java que reflete os valores da coluna ENUM('ativo', 'suspenso', 'desativado')
 * da tabela 'usuarios' no MySQL. O uso de nomes em minúsculas atende à convenção técnica
 * definida na arquitetura do projeto e simplifica a conversão bidirecional com o banco de dados.
 */
public enum StatusConta {
    ativo,
    suspenso,
    desativado
}
