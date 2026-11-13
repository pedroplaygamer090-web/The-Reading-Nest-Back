# Registro de Estado — Sessão 00: Implementação Inicial da API REST

Data da sessão: 13 de novembro de 2026  
Status da compilação: **Sucesso Total (BUILD SUCCESS - Java 25 / Spring Boot 4.1.2)**

---

## 1. O Que Foi Implementado

Implementação integral da API RESTful do **The Reading Nest** dividida estritamente nas seguintes camadas arquiteturais:

### 1.1 Configuração e Build (`pom.xml` & `application.properties`)
- Adicionadas ao `pom.xml` as dependências essenciais:
  - `org.springframework.boot:spring-boot-starter-jdbc` (acesso nativo e performático ao MySQL sem ORM).
  - `org.springframework.boot:spring-boot-starter-security` (hashing BCrypt e filtros de segurança).
  - `org.springframework.boot:spring-boot-starter-jackson` (serialização e anotações Jackson).
  - `org.springframework.data:spring-data-commons` (suporte à paginação global com `Page`, `Pageable` e `PageImpl`).
- `application.properties` configurado para carregar credenciais do MySQL via `${SPRING_DATASOURCE_PASSWORD:}` e desabilitar execução descontrolada de scripts DDL com `spring.sql.init.mode=never`.

### 1.2 Entidades de Domínio (`com.br.The_Reading_Nest_Back.entities`)
Java `record`s imutáveis e enums tipados sem acento:
- `StatusLivro.java`: enum com `ativo` e `desativado`.
- `StatusConta.java`: enum com `ativo`, `suspenso` e `desativado`.
- `StatusAluguel.java`: enum com `ativo`, `concluido` e `atrasado`.
- `Genero.java`: record contendo `id` (UUID v4) e `nome`.
- `Livro.java`: record contendo `id`, `generosId`, `titulo`, `autor`, `descricao`, `anoPublicacao`, `isbn`, `quantidadeDisponivel` e `statusLivro`.
- `Usuario.java`: record contendo `id`, `nome`, `email`, `@JsonIgnore String senha` e `statusConta`.
- `Aluguel.java`: record contendo `id`, `usuariosId`, `dataAluguel`, `dataDevolucaoPrevista` e `status`.
- `DetalheAluguel.java`: record associativo contendo `id`, `alugueisId` e `livrosId`.

### 1.3 Objetos de Transferência de Dados (`com.br.The_Reading_Nest_Back.dtos`)
DTOs imutáveis com validações do Jakarta Bean Validation (`jakarta.validation.constraints`):
- `GeneroRequestDTO` e `GeneroResponseDTO`
- `LivroRequestDTO` e `LivroResponseDTO`
- `UsuarioRequestDTO`, `UsuarioUpdateDTO` e `UsuarioResponseDTO` (sem campo de senha)
- `AluguelRequestDTO`, `AluguelUpdateDTO`, `AluguelCancelamentoDTO`, `AluguelResumoResponseDTO` e `AluguelDetalhadoResponseDTO`
- `ErrorResponseDTO` (`status`, `erros`, `horario`) padronizando o retorno de falhas

### 1.4 Tratamento Centralizado de Exceções (`com.br.The_Reading_Nest_Back.exceptions`)
- `CustomException.java`: desacopla regras de negócio com `HttpStatus` e lista imutável de erros.
- `GlobalExceptionHandler.java`: intercepta 100% dos erros HTTP e de negócio (`MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `NoResourceFoundException`, `HttpRequestMethodNotSupportedException`, `CustomException`, `DataIntegrityViolationException`, `Exception`), retornando o formato único `ErrorResponseDTO` e registrando erros genéricos com `log.error`.

### 1.5 Repositórios com SQL Puro (`com.br.The_Reading_Nest_Back.repositories`)
Acesso a dados estritamente via `JdbcTemplate` e `RowMapper`, sem `SELECT *`:
- `GeneroRepository`: CRUD paginado, validações de duplicidade insensível a maiúsculas (`existsByNome`, `existsByNomeAndIdNot`).
- `LivroRepository`: busca paginada com junção dinâmica (`INNER JOIN generos`), consulta por ID, decremento atômico de estoque (`UPDATE livros SET quantidade_disponivel = quantidade_disponivel - 1 WHERE id = ? AND quantidade_disponivel > 0`), incremento de estoque e soft-delete.
- `UsuarioRepository`: consultas seguras que nunca projetam o atributo `senha` nas listagens/leituras comuns, validação de e-mail único e atualização parcial.
- `AluguelRepository`: gravação de cabeçalho e detalhes associativos, busca paginada de aluguéis não concluídos ordenada por prazo, histórico do usuário por ID e consulta detalhada agregada com livros.

### 1.6 Mappers (`com.br.The_Reading_Nest_Back.mappers`)
Componentes Spring `@Component` isolando as camadas de apresentação e persistência:
- `GeneroMapper`, `LivroMapper`, `UsuarioMapper`, `AluguelMapper`.

### 1.7 Regras de Negócio e Transações (`com.br.The_Reading_Nest_Back.services`)
- `GeneroService`: validação de unicidade de nome, geração de UUID v4 e integridade referencial.
- `LivroService`: validação de chave estrangeira de gênero, unicidade de ISBN, soft-delete no `DELETE`.
- `UsuarioService`: unicidade de e-mail, hashing unidirecional de senhas com `BCryptPasswordEncoder` e soft-delete.
- `AluguelService`: criação atômica com validação de status do usuário (ativo), status dos livros (ativo), decremento atômico do estoque com prevenção de concorrência, atualização de prazo/status com reposição de estoque, e devolução/cancelamento com logging de justificativa.

### 1.8 Controladores REST (`com.br.The_Reading_Nest_Back.controllers`)
Endpoints REST no plural com validação `@Valid` e paginação com `Pageable`:
- `GeneroController` (`/generos`)
- `LivroController` (`/livros`)
- `UsuarioController` (`/usuarios`)
- `AluguelController` (`/alugueis`)

### 1.9 Segurança e Web MVC (`com.br.The_Reading_Nest_Back.security` e `config`)
- `SecurityConfig`: Spring Security 7 Lambda DSL, sessão `STATELESS`, CSRF desabilitado, bean `PasswordEncoder` (`BCryptPasswordEncoder`), bean `CorsConfigurationSource` liberando `http://127.0.0.1:5500` e `http://localhost:5500` (Live Server) prevenindo bloqueios de preflight OPTIONS, e liberação temporária de todas as rotas (`.anyRequest().permitAll()`).
- `CorsConfig`: mapeamento global WebMvc CORS para portas 5500.
- `WebConfig`: conversor case-insensitive para os enumeradores (`StatusLivro`, etc.) e definição de limite de paginação global padrão 30 e máximo 100 itens.

---

## 2. Decisões Arquiteturais e Motivos

1. **`JdbcTemplate` sem ORM/JPA:**
   Garante performance máxima, queries com plano de execução transparente e evita problemas de lazy loading ou proxies n-para-n.
2. **Atualização condicional atômica de estoque no MySQL:**
   O comando `UPDATE livros SET quantidade_disponivel = quantidade_disponivel - 1 WHERE id = ? AND quantidade_disponivel > 0` resolve a condição de corrida em nível de lock de linha do banco, impedindo que o estoque fique negativo (-1) sob requisições simultâneas concorrentes.
3. **Não exposição do atributo `senha`:**
   A coluna `senha` não é trazida nas consultas de leitura pública do `UsuarioRepository` e a classe `UsuarioResponseDTO` não possui o campo, blindando completamente a API contra vazamento estrutural.
4. **Padronização das anotações Jackson:**
   No Jackson 3 (FasterXML), o ecossistema mantém as anotações no pacote oficial `com.fasterxml.jackson.annotation` por compatibilidade cruzada, viabilizando o uso seguro de `@JsonIgnore`.
5. **CORS duplo (Security + MVC):**
   A declaração do `CorsConfigurationSource` no Spring Security intercepta as requisições `OPTIONS` de preflight do Live Server antes da cadeia de filtros de autenticação, evitando rejeições indesejadas pelo navegador.

---

## 3. O Que Foi Testado e Resultado

- **Compilação Completa do Maven:**
  - Comando: `.\mvnw.cmd clean compile`
  - Resultado: `[INFO] Compiling 43 source files with javac [debug parameters release 25] to target\classes` -> `[INFO] BUILD SUCCESS` (0 erros).
- **Inspeção de Dependências:**
  - Dependências de `spring-boot-starter-jdbc`, `spring-boot-starter-security`, `spring-boot-starter-jackson` e `spring-data-commons` integradas e resolvidas no Java 25.

---

## 4. O Que Ficou Fora do Escopo (Próximos Passos)

- Implementação do endpoint de autenticação e geração de tokens JWT (`/auth/login`).
- Configuração de roles e restrição de acesso por perfil (`ROLE_ADMIN`, `ROLE_CLIENTE`) nos endpoints após ativação do login.
- Migração automatizada com Flyway/Liquibase caso acordado no futuro.
