# Plano de Finalização — The Reading Nest Backend

> **REGRA DE ESCOPO OBRIGATÓRIA:** Não leia nem execute instruções dos arquivos `spec.md`, `REVIEW.md`, `DATABASE.md` ou `prompt.md` antigos. O projeto base já está implementado e compilando. Execute **exclusivamente** as atualizações, adições e revisões descritas neste arquivo `finalizacao.md`.

### Objetivo Desta Etapa
Atualizar e finalizar o backend do **The Reading Nest** (Java 25 + Spring Boot 4.1.2 + JdbcTemplate + MySQL `the-reading-nest_db`):
1. Adicionar o suporte completo à coluna **`perfil`** (`ENUM('admin', 'cliente')`) já existente na tabela `usuarios`.
2. Implementar a **Autenticação e Autorização real com JWT** (`POST /auth/login`, token Bearer e bloqueio de rotas por perfil `admin` vs `cliente`).
3. Criar o arquivo **`src/main/resources/schema.sql`** com o DDL oficial e atualizado das 5 tabelas.
4. Auditar todo o projeto em busca de erros e gerar o **Relatório Completo** explicado sobre tudo o que existe no backend.

---

### 1. Regras Técnicas Rígidas (Manter o Padrão do Projeto)
- **Comentários Duplos:** Todos os arquivos `.java` novos ou alterados devem conter comentários em **português brasileiro** com duas camadas:
    1. **Explicação simples (para iniciantes):** O que aquele código faz na prática no sistema.
    2. **Explicação técnica:** Como e por que funciona tecnicamente por baixo dos panos.
- **Stack Spring Boot 4.1.2 / Java 25:**
    - Proibido usar JPA/Hibernate (`jakarta.persistence.*`). Apenas `JdbcTemplate` e `RowMapper`.
    - Proibido usar `javax.*` ou `com.fasterxml.jackson.*`. Apenas `jakarta.*` e `tools.jackson.*`.
    - Proibido usar `SELECT *` em qualquer consulta SQL.
- **Não tocar no Git:** Não execute nenhum comando `git` (`add`, `commit`, `config`, etc.).

---

### 2. Dependência JWT (`pom.xml`), `application.properties` e `schema.sql`
- **Dependência JWT (`pom.xml`):** Adicione a biblioteca **JJWT** (`io.jsonwebtoken:jjwt-api`, `io.jsonwebtoken:jjwt-impl` e `io.jsonwebtoken:jjwt-jackson` na versão `0.12.6`) para geração e validação de tokens JWT no Java 25.
- **Configuração (`src/main/resources/application.properties`):** Mantenha todas as configurações atuais intactas (incluindo `spring.sql.init.mode=never`) e adicione apenas estas duas linhas ao final:
    - `jwt.secret=${JWT_SECRET:TheReadingNestChaveSecretaSuperSeguraParaAssinaturaJWT2026!}`
    - `jwt.expiration=${JWT_EXPIRATION:86400000}`
- **Arquivo `src/main/resources/schema.sql` *(NOVO)*:** Crie o script SQL oficial e idempotente (`CREATE TABLE IF NOT EXISTS`) das 5 tabelas do banco `the-reading-nest_db`:
    - `generos` (`id`, `nome`)
    - `livros` (`id`, `generos_id`, `titulo`, `autor`, `descricao`, `ano_publicacao`, `isbn`, `quantidade_disponivel`, `status_livro`)
    - `usuarios` (`id`, `nome`, `email`, `senha`, `perfil ENUM('admin', 'cliente') NOT NULL DEFAULT 'cliente'`, `status_conta ENUM('ativo', 'suspenso', 'desativado') NOT NULL DEFAULT 'ativo'`)
    - `alugueis` (`id`, `usuarios_id`, `data_aluguel`, `data_devolucao_prevista`, `status`)
    - `detalhes_alugueis` (`id`, `alugueis_id`, `livros_id`)

---

### 3. Atualização Completa da Coluna `perfil` (`admin` / `cliente`)

Atualize todas as camadas de usuários para refletir a coluna `perfil ENUM('admin', 'cliente') NOT NULL DEFAULT 'cliente'` já existente no banco de dados:

- **`entities/PerfilUsuario.java`** *(NOVO)*:
    - Enum em minúsculas sem acento: `admin`, `cliente`.
- **`entities/Usuario.java`** *(ATUALIZAR)*:
    - `public record Usuario(String id, String nome, String email, @JsonIgnore String senha, PerfilUsuario perfil, StatusConta statusConta)` (mantendo `@JsonIgnore` de `tools.jackson.annotation.JsonIgnore` na senha).
- **`dtos/UsuarioRequestDTO.java`** *(ATUALIZAR)*:
    - Manter `nome`, `email` e `senha` com suas validações `jakarta.validation.constraints`.
    - Adicionar `PerfilUsuario perfil` (campo opcional na requisição).
- **`dtos/UsuarioUpdateDTO.java`** *(ATUALIZAR)*:
    - Incluir `PerfilUsuario perfil` (opcional) junto com `nome`, `email`, `senha` e `statusConta`.
- **`dtos/UsuarioResponseDTO.java`** *(ATUALIZAR)*:
    - `public record UsuarioResponseDTO(String id, String nome, String email, PerfilUsuario perfil, StatusConta statusConta)` (sem o campo `senha`).
- **`repositories/UsuarioRepository.java`** *(ATUALIZAR)*:
    - Atualizar `insert`, `findAll`, `findById` e `update` para selecionar, inserir e atualizar explicitamente a coluna `perfil` (sem usar `SELECT *` e sem retornar `senha` nas consultas comuns).
    - Adicionar o método `findByEmailComSenha(String email)` retornando `Optional<Usuario>` com `SELECT id, nome, email, senha, perfil, status_conta FROM usuarios WHERE email = ?`, utilizado exclusivamente pelo serviço de login.
- **`mappers/UsuarioMapper.java`** e **`services/UsuarioService.java`** *(ATUALIZAR)*:
    - Na criação de usuário (`UsuarioService.criarUsuario`), se `perfil` vier `null` (cadastro público), definir automaticamente `PerfilUsuario.cliente`.
    - Caso alguém envie `perfil = admin` na criação ou edição, validar se quem está fazendo a requisição está autenticado com papel `ROLE_ADMIN`; caso não esteja (ex: visitante se cadastrando), forçar `PerfilUsuario.cliente` ou lançar `CustomException` com `403 Forbidden` (`"APENAS ADMINISTRADORES PODEM DEFINIR PERFIL ADMIN"`).

---

### 4. Implementação do Login e Geração de Token JWT

Crie os DTOs, o serviço de tokens, a regra de autenticação e o controller de login:

- **`dtos/LoginRequestDTO.java`** *(NOVO)*:
    - `@NotBlank(message = "O e-mail é obrigatório") @Email(message = "Formato de e-mail inválido") String email`
    - `@NotBlank(message = "A senha é obrigatória") String senha`
- **`dtos/LoginResponseDTO.java`** *(NOVO)*:
    - `String token`, `String tipo` (valor `"Bearer"`), `UsuarioResponseDTO usuario` (retornando os dados seguros do usuário logado, incluindo seu `perfil`, para o frontend armazenar).
- **`security/JwtService.java`** *(NOVO)*:
    - Componente Spring (`@Service`) que lê `jwt.secret` e `jwt.expiration` do `application.properties`.
    - `gerarToken(Usuario usuario)`: Gera um token JWT assinado com HMAC-SHA256 contendo `subject` = `usuario.email()`, e *claims* extras `id` (`usuario.id()`), `nome` (`usuario.nome()`) e `perfil` (`usuario.perfil().name()`), com data de emissão e expiração.
    - `extrairEmail(String token)` e `isTokenValido(String token)`: Valida assinatura, integridade e data de expiração do token sem lançar exceções não tratadas.
- **`services/AuthService.java`** *(NOVO)*:
    - Método `login(LoginRequestDTO dto)`:
        1. Busca o usuário via `usuarioRepository.findByEmailComSenha(dto.email())`. Se não encontrar, lança `CustomException` com `401 Unauthorized` (`"E-MAIL OU SENHA INVÁLIDOS"`).
        2. Compara a senha enviada com o hash salvo usando `passwordEncoder.matches(dto.senha(), usuario.senha())`. Se não bater, lança `CustomException` com `401 Unauthorized` (`"E-MAIL OU SENHA INVÁLIDOS"`).
        3. Verifica se `usuario.statusConta() == StatusConta.ativo`. Se estiver `suspenso` ou `desativado`, lança `CustomException` com `403 Forbidden` (`"CONTA SUSPENSA OU DESATIVADA. ENTRE EM CONTATO COM A BIBLIOTECA"`).
        4. Gera o token via `jwtService.gerarToken(usuario)` e retorna `LoginResponseDTO("Bearer", token, usuarioMapper.toResponseDTO(usuario))`.
- **`controllers/AuthController.java`** *(NOVO)*:
    - `@RestController` com `@RequestMapping("/auth")`.
    - `POST /auth/login`: Recebe `@Valid @RequestBody LoginRequestDTO dto` e retorna `ResponseEntity.ok(authService.login(dto))`.

---

### 5. Filtro JWT, Autorização por Perfil e Proteção de Dono

Atualize a camada de segurança e os serviços para que cada rota respeite o perfil do usuário (`admin` ou `cliente`):

- **`security/JwtAuthenticationFilter.java`** *(NOVO)*:
    - Estende `OncePerRequestFilter`.
    - Lê o cabeçalho HTTP `Authorization`. Se começar com `"Bearer "`, extrai o token e valida com `JwtService`.
    - Busca o usuário no `UsuarioRepository` (via `findByEmailComSenha`). Se o usuário existir e estiver com `statusConta == StatusConta.ativo`, cria um `UsernamePasswordAuthenticationToken` colocando o `Usuario` como `principal` e a autoridade `ROLE_ADMIN` (se `perfil == PerfilUsuario.admin`) ou `ROLE_CLIENTE` (se `perfil == PerfilUsuario.cliente`), registrando no `SecurityContextHolder`.
- **`security/CustomAuthenticationEntryPoint.java` e `security/CustomAccessDeniedHandler.java`** *(NOVOS)*:
    - Garantem que qualquer falha de autenticação (`401 Unauthorized` — token ausente ou expirado) ou de autorização (`403 Forbidden` — cliente tentando acessar rota de admin) devolva **exclusivamente** o JSON padronizado `ErrorResponseDTO` (`status`, `erros`, `horario`) usando Jackson 3 (`tools.jackson.databind.ObjectMapper`), sem retornar página HTML ou corpo vazio.
- **`security/SecurityConfig.java`** *(ATUALIZAR)*:
    - Mantém CORS liberado para o Live Server (`http://127.0.0.1:5500` e `http://localhost:5500`), CSRF desabilitado, sessão `STATELESS` e o filtro `JwtAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`.
    - Registra `.exceptionHandling(...)` apontando para `CustomAuthenticationEntryPoint` (`401`) e `CustomAccessDeniedHandler` (`403`).
    - Regras exatas de autorização:
        - **Públicas (`permitAll()`):** `OPTIONS /**`, `POST /auth/login`, `POST /usuarios`, `GET /generos`, `GET /generos/{id}`, `GET /livros`, `GET /livros/{id}`.
        - **Exclusivas de Administrador (`hasRole("ADMIN")`):** `POST /generos`, `PUT /generos/{id}`, `DELETE /generos/{id}`, `POST /livros`, `PUT /livros/{id}`, `DELETE /livros/{id}`, `GET /usuarios`, `DELETE /usuarios/{id}`, `GET /alugueis`, `PUT /alugueis/{id}`.
        - **Autenticadas (`authenticated()`):** `GET /usuarios/{id}`, `GET /usuarios/{id}/alugueis`, `PUT /usuarios/{id}`, `POST /alugueis`, `GET /alugueis/{id}`, `POST /alugueis/{id}/devolver`, `POST /alugueis/{id}/cancelar`.
- **Validação de Dono nos Services (`UsuarioService` e `AluguelService`)** *(ATUALIZAR)*:
    - Crie um método auxiliar de segurança (ex: `SecurityUtils` no pacote `security`) para recuperar o `Usuario` autenticado do `SecurityContextHolder`.
    - Se o usuário autenticado tiver `perfil == PerfilUsuario.cliente`:
        - Em `UsuarioService` (`buscarPorId`, `atualizarUsuario`) e `AluguelService` (`listarPorUsuario`, `criarAluguel`): só permite operar se `usuarioLogado.id().equals(idAlvo)`; caso contrário, lança `CustomException` com `403 Forbidden` (`"ACESSO NEGADO: VOCÊ SÓ PODE ACESSAR SEUS PRÓPRIOS DADOS"`).
        - Em `AluguelService` (`buscarDetalhado`, `devolverAluguel`, `cancelarAluguel`): só permite operar se `aluguel.usuariosId().equals(usuarioLogado.id())`; caso contrário, lança `CustomException` com `403 Forbidden` (`"ACESSO NEGADO: ESTE ALUGUEL PERTENCE A OUTRO USUÁRIO"`).
        - Em `UsuarioService.atualizarUsuario`: se for `cliente`, não permite alterar o próprio `perfil` para `admin` nem reativar uma conta suspensa.
    - Se o usuário autenticado tiver `perfil == PerfilUsuario.admin`, tem acesso irrestrito a todas essas operações.

---

### 6. Auditoria Geral do Projeto em Busca de Erros
Antes de finalizar, faça uma varredura completa em **todos os arquivos existentes** nos 9 pacotes (`config`, `controllers`, `dtos`, `entities`, `exceptions`, `mappers`, `repositories`, `security`, `services`) e corrija imediatamente qualquer problema encontrado:
1. **Conferência de SQL e Colunas:** Verifique se todas as queries nos repositories usam nomes exatos do `src/main/resources/schema.sql`, sem `SELECT *`, e se o `UsuarioRepository` lê e grava `perfil` e `status_conta` sem erros.
2. **Conferência de Imports e Jackson 3:** Garanta que nenhum arquivo importe `javax.*`, `com.fasterxml.jackson.*` ou `jakarta.persistence.*`. Use exclusivamente `jakarta.*` e `tools.jackson.*`.
3. **Conferência de Exceções e Logs:** Confirme que o `GlobalExceptionHandler` trata `AccessDeniedException` e `AuthenticationException` (caso sejam lançadas dentro de métodos) além de todas as exceções já mapeadas, retornando sempre `ErrorResponseDTO`.
4. **Build de Validação:** Execute `.\mvnw.cmd clean compile` no terminal e corrija qualquer erro ou aviso até que a compilação termine 100% limpa no Java 25 e Spring Boot 4.1.2.

---

### 7. Entregáveis Obrigatórios ao Final (Em Arquivo e no Chat)
1. **Relatório Completo do Backend (`RELATORIO-BACKEND.md` na raiz do projeto):**
    - Crie o arquivo `RELATORIO-BACKEND.md` (e mostre um resumo claro no chat) contendo uma explicação completa, didática e organizada sobre **tudo o que existe no backend**, explicando:
        - **Como o sistema funciona de ponta a ponta:** O caminho de uma requisição desde o navegador (Live Server) -> Filtro JWT -> Controller -> Service -> Repository -> MySQL -> e a resposta JSON de volta.
        - **Mapa completo de todos os pacotes e classes:** O que cada pasta (`config`, `controllers`, `dtos`, `entities`, `exceptions`, `mappers`, `repositories`, `security`, `services`) faz e a função de cada arquivo `.java` criado no projeto.
        - **Tabela de todas as rotas da API:** Método HTTP, URL, quem tem permissão (`Público`, `Cliente Logado` ou `Apenas Admin`), o que espera receber e o que retorna.
        - **Segurança e Regras de Negócio:** Como funciona o login com JWT, a criptografia BCrypt, a trava de estoque no aluguel e o padrão de mensagens de erro.
2. **Registro de Estado:** Crie o arquivo `.agents/estado/01-perfil-jwt-auditoria.md` documentando as alterações feitas nesta sessão, **sem executar nenhum comando git**.
3. **Script SQL de Seed Atualizado (`INSERT IGNORE`):** Entregue no chat o bloco SQL atualizado para rodar no phpMyAdmin já preenchendo a coluna `perfil` (`'admin'` para o administrador e `'cliente'` para os 3 clientes) junto com os gêneros, livros e o aluguel de exemplo.
4. **Exemplos de Integração Frontend (`fetch` com JWT):** Entregue no chat exemplos completos em JavaScript puro mostrando:
    - Como chamar `POST /auth/login`, salvar o `token` e os dados do `usuario` (incluindo `usuario.perfil`) no `localStorage`.
    - Como enviar o cabeçalho `"Authorization": "Bearer " + localStorage.getItem("token")` nas requisições protegidas (como alugar livro, ver `GET /usuarios/{id}/alugueis` ou cadastrar livro como admin).
