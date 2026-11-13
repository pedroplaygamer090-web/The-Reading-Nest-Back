# Registro de Estado — Sessão 01: Perfil de Usuário, Autenticação JWT e Auditoria Geral

Data da sessão: 13 de novembro de 2026  
Status da compilação: **Sucesso Total (BUILD SUCCESS - Java 25 / Spring Boot 4.1.2)**

---

## 1. O Que Foi Implementado

Finalização e blindagem do backend do **The Reading Nest**, dividida nas seguintes camadas e módulos:

### 1.1 Dependências e Configuração de Ambiente
- Adicionadas ao `pom.xml` as bibliotecas **JJWT 0.12.6** (`io.jsonwebtoken:jjwt-api`, `io.jsonwebtoken:jjwt-impl` e `io.jsonwebtoken:jjwt-jackson`) compatíveis com Java 25.
- Atualizado `src/main/resources/application.properties` com as chaves `jwt.secret` (segredo HMAC-SHA256) e `jwt.expiration` (validade de 24h), preservando intacta a propriedade `spring.sql.init.mode=never`.
- Criado o script DDL oficial e idempotente `src/main/resources/schema.sql` com as 5 tabelas (`generos`, `livros`, `usuarios`, `alugueis`, `detalhes_alugueis`) e o índice composto `idx_alugueis_status_devolucao` em `(status, data_devolucao_prevista)`.

### 1.2 Camada de Domínio e Modelo de Usuários
- Criado o enum `PerfilUsuario.java` com valores em minúsculas sem acento (`admin`, `cliente`).
- Atualizada a entidade `Usuario.java` para incluir o atributo `PerfilUsuario perfil`.
- Atualizados os DTOs `UsuarioRequestDTO`, `UsuarioUpdateDTO` e `UsuarioResponseDTO` com suporte ao perfil e ocultação permanente de credenciais.
- Atualizado `UsuarioMapper.java` para converter e propagar o perfil com fallback padrão para `cliente`.
- Atualizado `UsuarioRepository.java` com projeção explícita da coluna `perfil` em todas as queries e adição do método `findByEmailComSenha(String email)` exclusivo para o fluxo de autenticação.
- Atualizado `WebConfig.java` com conversor case-insensitive para o enum `PerfilUsuario`.

### 1.3 Fluxo de Autenticação e Autorização JWT
- Criados os DTOs `LoginRequestDTO` e `LoginResponseDTO`.
- Criado o serviço `JwtService.java` para geração, extração de claims e validação resiliente de tokens HMAC-SHA256.
- Criado `AuthService.java` com validação de credenciais via `PasswordEncoder.matches`, bloqueio de contas com status diferente de `ativo` e emissão de token.
- Criado `AuthController.java` expondo a rota pública `POST /auth/login`.
- Criado `JwtAuthenticationFilter.java` interceptando cabeçalhos `Authorization: Bearer <token>` e registrando `ROLE_ADMIN` ou `ROLE_CLIENTE` no `SecurityContextHolder`.
- Criados `CustomAuthenticationEntryPoint.java` e `CustomAccessDeniedHandler.java` devolvendo o JSON padronizado `ErrorResponseDTO` via Jackson 3 (`tools.jackson.databind.ObjectMapper`) para códigos HTTP 401 e 403.
- Atualizado `SecurityConfig.java` com regras estritas de autorização (rotas públicas, rotas exclusivas de admin e rotas autenticadas).

### 1.4 Regras de Negócio e Validação de Propriedade (Dono)
- Criado `SecurityUtils.java` para recuperação tipada do `Usuario` autenticado.
- Em `UsuarioService`:
  - Criação de contas com `perfil = admin` permitida exclusivamente para administradores autenticados (visitantes recebem `cliente`).
  - Clientes comuns só podem consultar e atualizar o seu próprio registro (`idAlvo.equals(usuarioLogado.id())`), com bloqueio 403 em tentativas de adulteração de perfil para admin ou reativação de conta.
- Em `AluguelService`:
  - Clientes comuns só podem listar seus próprios aluguéis, criar aluguéis em seu próprio nome e consultar, devolver ou cancelar aluguéis pertencentes a si.
  - Administradores mantêm acesso global e irrestrito.

### 1.5 Tratamento de Exceções e Auditoria Geral
- Atualizado `GlobalExceptionHandler.java` para capturar `AccessDeniedException` (HTTP 403) e `AuthenticationException` (HTTP 401) com imports estritos do Spring Security, mantendo a conformidade com `ErrorResponseDTO`.
- Conduzida varredura geral em todos os pacotes:
  - Eliminação de queries `SELECT *` (todas as colunas são explicitamente listadas).
  - Remoção de imports legados `com.fasterxml.jackson.annotation.JsonIgnore` substituído por anotação tipada correspondente no Java 25.
  - Inclusão sistemática de comentários duplos (didático para iniciantes + explicação técnica por baixo dos panos) em 100% dos arquivos novos e alterados.

---

## 2. Decisões Arquiteturais

1. **Autenticação Stateless e Bearer Token:**
   - Adotou-se arquitetura REST pura com token JWT sem estado no servidor, permitindo que o frontend (Live Server / SPA) envie o token em cada requisição através do cabeçalho `Authorization: Bearer <token>`.
2. **Defesa em Profundidade (SecurityConfig + Services):**
   - O `SecurityConfig` realiza a filtragem primária de permissão no nível de rota HTTP (bloqueando clientes que tentem acessar endpoints exclusivos de administração).
   - Os serviços (`UsuarioService` e `AluguelService`) realizam a filtragem de granularidade fina através da checagem de dono (*owner validation*), impedindo que um cliente autenticado altere recursos de outro cliente mesmo que o verbo e a URL sejam permitidos.
3. **Respostas Padronizadas de Segurança:**
   - Os handlers `CustomAuthenticationEntryPoint` e `CustomAccessDeniedHandler` garantem que qualquer erro de segurança seja serializado no mesmo formato JSON `ErrorResponseDTO` adotado em todo o projeto, evitando páginas HTML padrão de erro do servlet.

---

## 3. O Que Foi Testado e o Resultado

- **Compilação Geral (`.\mvnw.cmd clean compile`):**
  - **Resultado:** `BUILD SUCCESS` limpo, sem erros e sem advertências críticas, compilando 53 arquivos de código-fonte no Java 25 com Spring Boot 4.1.2.
- **Compilação de Testes (`.\mvnw.cmd test-compile`):**
  - **Resultado:** `BUILD SUCCESS`.
- **Inspeção Estática de Conformidade de Pacotes e Consultas:**
  - Ausência de `SELECT *` confirmada em 100% dos repositórios.
  - Ausência de pacotes `javax.*` legados de servlet/naming/persistence confirmada.
  - Ausência de bibliotecas ORM/JPA confirmada.

---

## 4. O Que Ficou Fora do Escopo (Próximos Passos)

- Implementação do frontend (HTML, CSS e JavaScript integrados via `fetch`).
- Testes automatizados de integração com MockMvc e container de testes para cenários de concorrência.
