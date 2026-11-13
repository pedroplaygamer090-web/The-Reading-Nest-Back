# Sessão 02 — Auditoria de compilação, SQL e segurança

## Implementado

- **Entidade/serialização:** removido o import legado Jackson 2 de `Usuario`. As respostas HTTP de usuários continuam usando `UsuarioResponseDTO`, sem o campo `senha`.
- **DTOs/exceções:** confirmado que `ErrorResponseDTO` está declarado publicamente no pacote usado pelo `GlobalExceptionHandler` e pelos handlers do Spring Security.
- **Repositories/schema:** conferidas as colunas SQL em relação a `schema.sql`; sem `SELECT *` nas consultas.
- **Segurança:** `SecurityConfig`, `JwtAuthenticationFilter` e handlers permanecem compatíveis com Spring Boot 4.1.2 e Spring Security 7.

## Decisões

- Jackson 3.1.7 não disponibiliza `tools.jackson.annotation.JsonIgnore`; não foi mantida uma anotação inexistente nem reintroduzido um import Jackson 2. A entidade não é exposta pelos controllers, que retornam DTOs sem senha.
- `javax.crypto.SecretKey` foi mantido por pertencer à API Java SE; não é pacote legado Jakarta EE.
- A falha reportada para `ErrorResponseDTO` não se reproduziu após limpeza: nome do arquivo, record, pacote e imports são consistentes.

## Verificação

- `mvnw clean test`: sucesso; 53 fontes principais e o contexto de aplicação compilam/iniciam, teste existente aprovado (1 teste).
- Varredura de imports e queries: nenhum import `com.fasterxml.jackson.*` ou `jakarta.persistence.*`; nenhuma query com `SELECT *`.

## Fora do escopo

- Não foram alteradas outras mudanças já existentes na working tree.
- Não foram adicionados testes de integração contra MySQL; o contexto foi iniciado sem executar operações de banco.
