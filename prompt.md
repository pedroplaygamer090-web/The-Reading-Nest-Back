# Plano de Implementação — The Reading Nest Backend

Leia atentamente e siga estritamente todas as regras, restrições e especificações definidas nos arquivos `AGENTS.md`, `REVIEW.md`, `spec.md` e `DATABASE.md` presentes na raiz deste projeto.

### Tarefa Principal
Implemente o backend completo da aplicação de biblioteca **The Reading Nest** utilizando **Java 25** (Amazon Corretto), **Spring Boot 4.1.2**, acesso a dados puro via **JdbcTemplate** (sem JPA/Hibernate) e banco de dados **MySQL** (`the-reading-nest_db`), garantindo integração imediata com o frontend **The-Reading-Nest-Front** (que roda via **Live Server**).

---

### 1. Padrão Obrigatório de Comentários em Todas as Classes
Em todos os arquivos `.java` criados, escreva comentários claros em **português brasileiro** contendo sempre duas camadas de explicação:
1. **Explicação simples (para iniciantes):** O que aquele bloco, classe ou método faz na prática dentro do fluxo da biblioteca.
2. **Explicação técnica:** Como e por que a implementação funciona tecnicamente por baixo dos panos (ex: atomicidade de transação com `@Transactional`, prevenção de SQL Injection com queries parametrizadas `?`, imutabilidade dos Java `record`s, mapeamento de colunas via `RowMapper`).

---

### 2. Verificação de Dependências, Stack e Configurações Base
- **Dependências (`pom.xml` ou `build.gradle`):** Inspecione o arquivo de build imediatamente. Verifique se `spring-boot-starter-jdbc` e `spring-boot-starter-security` estão declarados (junto com `spring-boot-starter-web`, `spring-boot-starter-validation` e `mysql-connector-j`). Caso algum esteja ausente, adicione-o e baixe as dependências antes de gerar as classes.
- **Regras Estritas da Stack Spring Boot 4.1.2 / Java 25:**
    - Proibido usar JPA/Hibernate (`jakarta.persistence.*`). Todo acesso ao banco deve ser feito exclusivamente via `JdbcTemplate`.
    - Proibido usar pacotes legados `javax.*` ou `com.fasterxml.jackson.*`. Utilize estritamente `jakarta.*` (Jakarta EE 11) e `tools.jackson.*` (Jackson 3).
- **Configuração (`src/main/resources/application.properties`):** Mantenha a leitura via variáveis de ambiente sem expor credenciais:
    - `spring.application.name=the-reading-nest-back`
    - `spring.datasource.url=jdbc:mysql://localhost:3306/the-reading-nest_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
    - `spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}`
    - `spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}`
    - `spring.sql.init.mode=never`
    - `spring.threads.virtual.enabled=true`
    - `spring.data.web.pageable.default-page-size=30`
    - `spring.data.web.pageable.max-page-size=100`
- **SQL Explícito:** Proibido utilizar `SELECT *` em qualquer consulta. Todas as queries nos repositories devem listar explicitamente cada coluna.

---

### 3. Estrutura Exata do Banco de Dados (`the-reading-nest_db`)
As consultas SQL devem corresponder exatamente às 5 tabelas e colunas abaixo:
1. **`generos`**: `id` (`VARCHAR(36)` PK), `nome` (`VARCHAR(60)` UNIQUE NOT NULL).
2. **`livros`**: `id` (`VARCHAR(36)` PK), `generos_id` (`VARCHAR(36)` FK -> `generos.id`), `titulo` (`VARCHAR(150)` NOT NULL), `autor` (`VARCHAR(100)` NOT NULL), `descricao` (`TEXT` NULL), `ano_publicacao` (`INT` NULL), `isbn` (`VARCHAR(20)` UNIQUE NULL), `quantidade_disponivel` (`INT` NOT NULL DEFAULT 1), `status_livro` (`ENUM('ativo', 'desativado')` NOT NULL DEFAULT `'ativo'`).
3. **`usuarios`**: `id` (`VARCHAR(36)` PK), `nome` (`VARCHAR(100)` NOT NULL), `email` (`VARCHAR(100)` UNIQUE NOT NULL), `senha` (`VARCHAR(255)` NOT NULL), `status_conta` (`ENUM('ativo', 'suspenso', 'desativado')` NOT NULL DEFAULT `'ativo'`).
4. **`alugueis`**: `id` (`VARCHAR(36)` PK), `usuarios_id` (`VARCHAR(36)` FK -> `usuarios.id`), `data_aluguel` (`DATETIME` NOT NULL DEFAULT CURRENT_TIMESTAMP), `data_devolucao_prevista` (`DATE` NOT NULL), `status` (`ENUM('ativo', 'concluido', 'atrasado')` NOT NULL DEFAULT `'ativo'`).
5. **`detalhes_alugueis`**: `id` (`VARCHAR(36)` PK), `alugueis_id` (`VARCHAR(36)` FK -> `alugueis.id`), `livros_id` (`VARCHAR(36)` FK -> `livros.id`).

---

### 4. Pacotes `entities` e `dtos` (Inspecionar capitalização das pastas existentes)

#### Pacote `entities` (Java `record`s imutáveis e Enums sem acento)
- **`StatusLivro.java`**: Enum com valores `ativo`, `desativado`.
- **`StatusConta.java`**: Enum com valores `ativo`, `suspenso`, `desativado`.
- **`StatusAluguel.java`**: Enum com valores `ativo`, `concluido`, `atrasado`.
- **`Genero.java`**: `record Genero(String id, String nome)`
- **`Livro.java`**: `record Livro(String id, String generosId, String titulo, String autor, String descricao, Integer anoPublicacao, String isbn, Integer quantidadeDisponivel, StatusLivro statusLivro)`
- **`Usuario.java`**: `record Usuario(String id, String nome, String email, @JsonIgnore String senha, StatusConta statusConta)` usando `@JsonIgnore` de `tools.jackson.annotation.JsonIgnore`.
- **`Aluguel.java`**: `record Aluguel(String id, String usuariosId, LocalDateTime dataAluguel, LocalDate dataDevolucaoPrevista, StatusAluguel status)`
- **`DetalheAluguel.java`**: `record DetalheAluguel(String id, String alugueisId, String livrosId)`

#### Pacote `dtos` (Java `record`s com validações `jakarta.validation.constraints`)
- **`GeneroRequestDTO.java`**: `@NotBlank @Size(max = 60) String nome`
- **`GeneroResponseDTO.java`**: `String id, String nome`
- **`LivroRequestDTO.java`**: `@NotBlank String generoId`, `@NotBlank @Size(max = 150) String titulo`, `@NotBlank @Size(max = 100) String autor`, `String descricao`, `@Min(1000) Integer anoPublicacao`, `@Size(max = 20) String isbn`, `@NotNull @Min(0) Integer quantidadeDisponivel`, `StatusLivro statusLivro` (opcional na criação, assumindo `ativo` se nulo).
- **`LivroResponseDTO.java`**: `String id, String generoId, String generoNome, String titulo, String autor, String descricao, Integer anoPublicacao, String isbn, Integer quantidadeDisponivel, StatusLivro statusLivro`
- **`UsuarioRequestDTO.java`**: `@NotBlank @Size(max = 100) String nome`, `@NotBlank @Email @Size(max = 100) String email`, `@NotBlank @Size(min = 6) String senha`
- **`UsuarioUpdateDTO.java`**: `@NotBlank @Size(max = 100) String nome`, `@NotBlank @Email @Size(max = 100) String email`, `String senha` (opcional; se informada, validar mínimo de 6 caracteres e criptografar), `StatusConta statusConta`
- **`UsuarioResponseDTO.java`**: `String id, String nome, String email, StatusConta statusConta` (proibido conter senha).
- **`AluguelRequestDTO.java`**: `@NotBlank String usuarioId`, `@NotNull @Future LocalDate dataDevolucaoPrevista`, `@NotEmpty List<String> livrosIds`
- **`AluguelUpdateDTO.java`**: `LocalDate dataDevolucaoPrevista`, `StatusAluguel status`
- **`AluguelCancelamentoDTO.java`**: `String motivo` (opcional)
- **`AluguelResumoResponseDTO.java`**: `String id, String usuarioId, String usuarioNome, LocalDateTime dataAluguel, LocalDate dataDevolucaoPrevista, StatusAluguel status`
- **`AluguelDetalhadoResponseDTO.java`**: `String id, String usuarioId, String usuarioNome, LocalDateTime dataAluguel, LocalDate dataDevolucaoPrevista, StatusAluguel status, List<LivroResponseDTO> livros`
- **`ErrorResponseDTO.java`**: `int status, List<String> erros, LocalDateTime horario`

---

### 5. Pacotes `repositories`, `mappers` e `exceptions`

#### Pacote `repositories` (Exclusivamente `JdbcTemplate`, `RowMapper` e SQL explícito sem `SELECT *`)
- **`GeneroRepository.java`**:
    - `insert(Genero genero)`: `INSERT INTO generos (id, nome) VALUES (?, ?)`
    - `findAll(Pageable pageable)`: `SELECT id, nome FROM generos ORDER BY nome ASC LIMIT ? OFFSET ?` + query `SELECT COUNT(id) FROM generos`
    - `findById(String id)`: `SELECT id, nome FROM generos WHERE id = ?`
    - `existsByNome(String nome)` e `existsByNomeAndIdNot(String nome, String id)`
    - `update(Genero genero)`: `UPDATE generos SET nome = ? WHERE id = ?`
    - `deleteById(String id)`: `DELETE FROM generos WHERE id = ?`
- **`LivroRepository.java`**:
    - `insert(Livro livro)`: `INSERT INTO livros (id, generos_id, titulo, autor, descricao, ano_publicacao, isbn, quantidade_disponivel, status_livro) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`
    - `findAll(StatusLivro status, String generoId, Pageable pageable)`: Query dinâmica parametrizada com `INNER JOIN generos g ON l.generos_id = g.id` trazendo todas as colunas explícitas + `g.nome AS genero_nome`, aplicando filtros opcionais, `LIMIT ? OFFSET ?` e contagem total para retornar `Page<LivroResponseDTO>`.
    - `findById(String id)`: Query explícita com `INNER JOIN generos g ON l.generos_id = g.id` retornando `Optional<LivroResponseDTO>`.
    - `findEntityById(String id)`: Query explícita retornando `Optional<Livro>` para regras internas de serviço.
    - `existsByIsbn(String isbn)` e `existsByIsbnAndIdNot(String isbn, String id)`
    - `update(Livro livro)`: `UPDATE livros SET generos_id = ?, titulo = ?, autor = ?, descricao = ?, ano_publicacao = ?, isbn = ?, quantidade_disponivel = ?, status_livro = ? WHERE id = ?`
    - `updateStatus(String id, StatusLivro status)`: `UPDATE livros SET status_livro = ? WHERE id = ?`
    - `decrementarEstoque(String id)`: Executa `UPDATE livros SET quantidade_disponivel = quantidade_disponivel - 1 WHERE id = ? AND quantidade_disponivel > 0` retornando `int` (linhas afetadas).
    - `incrementarEstoque(String id)`: Executa `UPDATE livros SET quantidade_disponivel = quantidade_disponivel + 1 WHERE id = ?`.
- **`UsuarioRepository.java`**:
    - `insert(Usuario usuario)`: `INSERT INTO usuarios (id, nome, email, senha, status_conta) VALUES (?, ?, ?, ?, ?)`
    - `findAll(Pageable pageable)`: `SELECT id, nome, email, status_conta FROM usuarios ORDER BY nome ASC LIMIT ? OFFSET ?` (nunca selecionar `senha`) + query de contagem.
    - `findById(String id)`: `SELECT id, nome, email, status_conta FROM usuarios WHERE id = ?` (nunca selecionar `senha`).
    - `existsByEmail(String email)` e `existsByEmailAndIdNot(String email, String id)`
    - `update(Usuario usuario, boolean atualizarSenha)`: Atualiza `nome`, `email`, `status_conta` (e `senha` apenas se `atualizarSenha` for verdadeiro).
    - `updateStatus(String id, StatusConta status)`: `UPDATE usuarios SET status_conta = ? WHERE id = ?`
- **`AluguelRepository.java`**:
    - `insertAluguel(Aluguel aluguel)`: `INSERT INTO alugueis (id, usuarios_id, data_aluguel, data_devolucao_prevista, status) VALUES (?, ?, ?, ?, ?)`
    - `insertDetalheAluguel(DetalheAluguel detalhe)`: `INSERT INTO detalhes_alugueis (id, alugueis_id, livros_id) VALUES (?, ?, ?)`
    - `findNaoConcluidos(Pageable pageable)`: `SELECT a.id, a.usuarios_id, u.nome AS usuario_nome, a.data_aluguel, a.data_devolucao_prevista, a.status FROM alugueis a INNER JOIN usuarios u ON a.usuarios_id = u.id WHERE a.status IN ('ativo', 'atrasado') ORDER BY a.data_devolucao_prevista ASC, a.data_aluguel ASC LIMIT ? OFFSET ?` + contagem total.
    - `findByUsuarioId(String usuarioId, Pageable pageable)`: `SELECT a.id, a.usuarios_id, u.nome AS usuario_nome, a.data_aluguel, a.data_devolucao_prevista, a.status FROM alugueis a INNER JOIN usuarios u ON a.usuarios_id = u.id WHERE a.usuarios_id = ? ORDER BY a.data_aluguel DESC LIMIT ? OFFSET ?` + contagem total.
    - `findByIdComLivros(String id)`: Busca o cabeçalho do aluguel com `INNER JOIN usuarios` e busca os livros vinculados via `INNER JOIN detalhes_alugueis da ON l.id = da.livros_id INNER JOIN generos g ON l.generos_id = g.id WHERE da.alugueis_id = ?`, retornando `Optional<AluguelDetalhadoResponseDTO>`.
    - `findEntityById(String id)`: `SELECT id, usuarios_id, data_aluguel, data_devolucao_prevista, status FROM alugueis WHERE id = ?`
    - `findLivrosIdsByAluguelId(String aluguelId)`: `SELECT livros_id FROM detalhes_alugueis WHERE alugueis_id = ?`
    - `updateAluguel(String id, LocalDate dataDevolucaoPrevista, StatusAluguel status)`: `UPDATE alugueis SET data_devolucao_prevista = ?, status = ? WHERE id = ?`

#### Pacote `mappers` (Componentes Spring `@Component`)
- **`GeneroMapper.java`**, **`LivroMapper.java`**, **`UsuarioMapper.java`**, **`AluguelMapper.java`**: Convertem DTOs de entrada em Entities (recebendo o UUID gerado pelo Service) e convertem Entities/projeções nos respectivos DTOs de resposta sem acoplar entidades ao banco.

#### Pacote `exceptions`
- **`CustomException.java`**: Exceção `RuntimeException` contendo `HttpStatus status` e `List<String> erros` (com construtor de conveniência para mensagem única).
- **`GlobalExceptionHandler.java`**: Classe `@RestControllerAdvice` garantindo que **100% dos erros** retornem exclusivamente `ErrorResponseDTO` (`status`, `erros`, `horario`):
    - `MethodArgumentNotValidException`: extrai todas as mensagens de validação de campo em `List<String>` e retorna `400 Bad Request`.
    - `HttpMessageNotReadableException`: retorna `400 Bad Request` com `"CORPO DA REQUISIÇÃO INVÁLIDO OU FORMATO DE DATA INCORRETO"`.
    - `NoResourceFoundException`: retorna `404 Not Found` com `"ROTA NÃO ENCONTRADA"`.
    - `HttpRequestMethodNotSupportedException`: retorna `405 Method Not Allowed` com `"MÉTODO HTTP NÃO SUPORTADO PARA ESTA ROTA"`.
    - `CustomException`: retorna o `HttpStatus` e a lista de `erros` definidos na exceção.
    - `DataIntegrityViolationException`: retorna `409 Conflict` com `"OPERAÇÃO BLOQUEADA POR INTEGRIDADE DE DADOS NO BANCO (REGISTRO EM USO)"`.
    - `Exception`: registra obrigatoriamente `log.error("Erro interno inesperado no servidor", ex)` via SLF4J e retorna `500 Internal Server Error` com `"ERRO INTERNO NO SERVIDOR"`.

---

### 6. Pacotes `services`, `controllers`, `security` e `config`

#### Pacote `services` (Lógica de negócio, geração de UUIDs e `@Transactional`)
- **`GeneroService.java`**: CRUD completo de gêneros; valida nome duplicado (`409 Conflict`) e existência antes de editar/excluir (`404 Not Found`).
- **`LivroService.java`**: CRUD completo de livros; valida se `generoId` existe (`404 Not Found`), valida ISBN duplicado (`409 Conflict`), gera UUID na criação e aplica soft-delete (`status_livro = 'desativado'`) no `DELETE`.
- **`UsuarioService.java`**: CRUD completo de usuários; valida e-mail duplicado (`409 Conflict`), criptografa senha com `PasswordEncoder` (`BCryptPasswordEncoder`), gera UUID na criação e aplica soft-delete (`status_conta = 'desativado'`) no `DELETE`.
- **`AluguelService.java`**:
    - `criarAluguel(AluguelRequestDTO dto)` (`@Transactional`):
        1. Verifica se não há IDs de livros duplicados na lista `livrosIds` (`400 Bad Request`).
        2. Busca o usuário; valida se existe (`404 Not Found`) e se `statusConta == StatusConta.ativo` (`400 Bad Request`).
        3. Para cada `livroId`: verifica se existe (`404 Not Found`), se `statusLivro == StatusLivro.ativo` (`400 Bad Request`) e executa `livroRepository.decrementarEstoque(livroId)` garantindo que `rowsAffected == 1` (se for `0`, lança `400 Bad Request` informando livro indisponível em estoque).
        4. Gera UUID para o `Aluguel` e para cada `DetalheAluguel` e persiste tudo na mesma transação.
    - `listarNaoConcluidos(Pageable pageable)`: Delega para `aluguelRepository.findNaoConcluidos(pageable)`.
    - `listarPorUsuario(String usuarioId, Pageable pageable)`: Valida se o usuário existe (`404 Not Found`) e retorna seus aluguéis paginados.
    - `buscarDetalhado(String id)`: Retorna `AluguelDetalhadoResponseDTO` ou lança `404 Not Found`.
    - `atualizarAluguel(String id, AluguelUpdateDTO dto)` (`@Transactional`): Atualiza data prevista e/ou status. Se o status anterior não era `concluido` e o novo status for `concluido`, percorre os livros do aluguel e chama `livroRepository.incrementarEstoque(livroId)`.
    - `devolverAluguel(String id)` (`@Transactional`): Valida se o aluguel existe (`404 Not Found`) e se já não está `concluido` (`400 Bad Request`). Altera o status para `concluido` e incrementa +1 no estoque de cada livro vinculado.
    - `cancelarAluguel(String id, AluguelCancelamentoDTO dto)` (`@Transactional`): Valida se o aluguel existe (`404 Not Found`) e se está `ativo` ou `atrasado` (`400 Bad Request`). Altera o status para `concluido`, incrementa +1 no estoque de cada livro vinculado e registra o `motivo` no log caso informado pelo administrador.

#### Pacote `controllers` (Rotas REST estritamente no plural)
- **`GeneroController.java`** (`@RequestMapping("/generos")`): `POST`, `GET` (paginado), `PUT /{id}`, `DELETE /{id}`.
- **`LivroController.java`** (`@RequestMapping("/livros")`): `POST`, `GET` (paginado, com `@RequestParam(required = false) StatusLivro status` e `@RequestParam(required = false) String generoId`), `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.
- **`UsuarioController.java`** (`@RequestMapping("/usuarios")`): `POST`, `GET` (paginado), `GET /{id}`, `GET /{id}/alugueis` (paginado), `PUT /{id}`, `DELETE /{id}`.
- **`AluguelController.java`** (`@RequestMapping("/alugueis")`): `POST`, `GET` (paginado, não concluídos), `GET /{id}`, `PUT /{id}`, `POST /{id}/devolver`, `POST /{id}/cancelar` (com `@RequestBody(required = false) AluguelCancelamentoDTO dto`).

#### Pacote `security` e Pacote `config`
- **`SecurityConfig.java`** (`security`): Utiliza Lambda DSL do Spring Security 7, ativa `.cors(Customizer.withDefaults())`, desabilita CSRF (`.csrf(AbstractHttpConfigurer::disable)`), configura sessão `STATELESS`, libera todas as rotas temporariamente (`.anyRequest().permitAll()`) até implementarmos autenticação na próxima etapa, e expõe `@Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }`.
- **`CorsConfig.java`** (`config`): Implementa `WebMvcConfigurer` liberando `http://127.0.0.1:5500` e `http://localhost:5500` (Live Server) para todos os métodos (`GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`) e headers.
- **`WebConfig.java`** (`config`): Garante a configuração global de paginação padrão de 30 e máximo de 100 itens.

---

### 7. Entregáveis Obrigatórios ao Final da Execução
1. **Código Completo:** Crie todos os arquivos descritos acima com comentários duplos (explicação simples para iniciantes + explicação técnica) em português brasileiro, sem `TODOs` ou métodos vazios.
2. **Script SQL de Seed (`INSERT IGNORE`):** Entregue um bloco SQL idempotente pronto para rodar no **phpMyAdmin** dentro de `the-reading-nest_db`, inserindo:
    - 4 gêneros literários (ex: Fantasia, Ficção Científica, Romance, Tecnologia).
    - 6 livros variados vinculados a esses gêneros, com descrição completa, ano de publicação, ISBN e estoque disponível.
    - 1 usuário administrador (`admin@thereadingnest.com`) e 3 usuários clientes comuns, todos com senhas já criptografadas em hash BCrypt válido (informando qual é a senha em texto puro de teste para cada um).
    - 1 aluguel ativo de exemplo vinculado a um cliente e a 2 livros da tabela `detalhes_alugueis`.
3. **Guia Prático de Integração Frontend (`fetch`):** Entregue exemplos completos e comentados em JavaScript puro (`http://localhost:8080/...`) mostrando como conectar cada uma das páginas do **The-Reading-Nest-Front** (Página Inicial, Catálogo com filtro por gênero, Modal de Detalhes/Aluguel, Login/Cadastro, Minha Conta listando `GET /usuarios/{id}/alugueis` + devolução/cancelamento, e Painel Administrador) à API, demonstrando como ler `dados.content` na paginação e exibir `erro.erros` no tratamento de falhas.
4. **Registro de Estado:** Crie o arquivo `.agents/estado/00-implementacao-inicial-api.md` seguindo exatamente o formato exigido no `AGENTS.md`, **sem executar nenhum comando git**.
5. **Compilação Final:** Execute `./mvnw clean compile` (ou `./gradlew compileJava`) ao final para baixar as dependências adicionadas e garantir que todo o projeto compila sem nenhum erro no Java 25 e Spring Boot 4.1.2.
