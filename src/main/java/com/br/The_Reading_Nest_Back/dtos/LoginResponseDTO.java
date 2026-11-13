package com.br.The_Reading_Nest_Back.dtos;

/**
 * Explicação simples:
 * Resposta devolvida para a tela quando o usuário faz login com sucesso.
 * Contém o token de segurança (JWT), o tipo do token ('Bearer') e as informações seguras do usuário,
 * incluindo seu perfil de acesso (admin ou cliente) para o frontend saber o que exibir na tela.
 *
 * Explicação técnica:
 * DTO de saída da rota POST /auth/login.
 * Retorna o token assinado gerado pelo JwtService acompanhado do esquema de autorização HTTP ('Bearer')
 * e a projeção pública 'UsuarioResponseDTO' (sem senha), permitindo ao cliente SPA/frontend persistir
 * a sessão no localStorage e anexar o token no cabeçalho Authorization das requisições subsequentes.
 *
 * @param tipo Esquema de autorização HTTP (sempre 'Bearer').
 * @param token Token JWT codificado e assinado digitalmente com HMAC-SHA256.
 * @param usuario Dados públicos e seguros do usuário autenticado.
 */
public record LoginResponseDTO(
        String tipo,
        String token,
        UsuarioResponseDTO usuario
) {
}
