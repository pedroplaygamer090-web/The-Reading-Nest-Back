# Relatório Completo do Backend — The Reading Nest

Sistema de Gerenciamento de Biblioteca Digital e Físico desenvolvido com **Java 25**, **Spring Boot 4.1.2**, acesso direto a dados com **Spring JdbcTemplate** (sem ORM/JPA) e banco de dados relacional **MySQL** (schema `the-reading-nest_db`).

---

## 1. Funcionamento do Sistema de Ponta a Ponta

O fluxo de dados da aplicação adota o estilo arquitetural RESTful em camadas estritamente isoladas, operando em regime **STATELESS** com tokens **JWT (JSON Web Token)** assinados com algoritmo **HMAC-SHA256**:

```
[ Navegador / Live Server (Porta 5500) ]
                   │
                   ▼ (HTTP Request + Header Authorization: Bearer <token>)
       [ CorsConfig / CorsFilter ]
                   │
                   ▼
     [ JwtAuthenticationFilter ]  ──(Token Inválido ou Ausente em Rota Privada)──► [ CustomAuthenticationEntryPoint (401) ]
                   │
                   ▼ (Se Perfil Insuficiente) ──► [ CustomAccessDeniedHandler (403) ]
             [ Controllers ]
                   │ (Valida DTO com Jakarta Validation)
                   ▼
              [ Services ] ◄──(Checagem de Dono / Regras de Negócio / BCrypt / Estoque)
                   │
                   ▼
            [ Repositories ] ──(JdbcTemplate + Queries Parametrizadas Sem SELECT *)
                   │
                   ▼
             [ MySQL DB ] (`the-reading-nest_db`)
                   │
                   ▼ (RowMapper / Entidades Record)
              [ Services ]
                   │ (Mappers -> DTOs de Resposta Higienizados)
                   ▼
             [ Controllers ]
                   │
                   ▼ (JSON de Resposta / HTTP Status Code)
[ Navegador / Live Server (Porta 5500) ]
```

### Passo a Passo de uma Requisição:
1. **Origem:** O frontend (SPA ou páginas HTML/JS hospedadas no Live Server em `http://127.0.0.1:5500` ou `http://localhost:5500`) envia uma requisição HTTP.
2. **CORS:** O Spring Security e o `CorsConfig` interceptam preflight `OPTIONS` e validam os cabeçalhos autorizados (`Authorization`, `Content-Type`).
3. **Filtro JWT (`JwtAuthenticationFilter`):**
   - Inspeciona o cabeçalho `Authorization: Bearer <token>`.
   - Se presente e válido, valida assinatura e prazo de expiração via `JwtService`.
   - Localiza o usuário no banco, confirma se `status_conta == 'ativo'` e registra um `UsernamePasswordAuthenticationToken` no `SecurityContextHolder` com papel `ROLE_ADMIN` ou `ROLE_CLIENTE`.
4. **Segurança de Rota (`SecurityConfig`):**
   - Se o recurso exigir permissão específica (`hasRole("ADMIN")`) e o usuário for `ROLE_CLIENTE`, o `CustomAccessDeniedHandler` interrompe o fluxo imediatamente retornando `403 Forbidden` padronizado.
   - Se o recurso for privado e o token estiver ausente ou inválido, o `CustomAuthenticationEntryPoint` devolve `401 Unauthorized` padronizado.
5. **Controlador REST (`Controllers`):**
   - Recebe o corpo da requisição e valida restrições com anotações `jakarta.validation.constraints` (`@Valid`).
   - Falhas de validação são interceptadas pelo `GlobalExceptionHandler`, devolvendo `400 Bad Request` com array de mensagens legíveis.
6. **Camada de Serviço (`Services`):**
   - Centraliza 100% da inteligência e lógica de negócio.
   - Os Services trabalham com entidades e agregados de domínio, como `AluguelCompleto`, e não formatam diretamente contratos HTTP.
   - Aplica a **validação de dono**: clientes comuns só podem visualizar ou alterar dados e aluguéis vinculados ao seu próprio ID de usuário.
   - Executa transações atômicas com `@Transactional`.
7. **Camada de Acesso a Dados (`Repositories`):**
   - Realiza consultas e comandos no MySQL através de `JdbcTemplate` com placeholders `?` para eliminar qualquer vulnerabilidade de SQL Injection.
   - Projeta explicitamente todas as colunas SQL (proibido o uso de `SELECT *`).
   - Mapeia resultados via `RowMapper` para records Java imutáveis.
8. **Resposta Formatada:**
   - Os Controllers recebem as entidades ou agregados retornados pelos Services e chamam os Mappers para convertê-los em DTOs higienizados (sem senhas e sem dados confidenciais), que são serializados em JSON e entregues ao frontend com o código HTTP adequado (`200 OK`, `201 Created`, `204 No Content`).

---

## 2. Mapa Completo de Pacotes e Classes

O backend está organizado em 9 pacotes modulares no namespace `com.br.The_Reading_Nest_Back`, totalizando 54 classes Java:

### 2.1. `config` — Configurações Gerais da Aplicação
- **`CorsConfig.java`**: Configura a política de Cross-Origin Resource Sharing no Spring MVC, liberando requisições do frontend nas portas do Live Server (`5500`).
- **`WebConfig.java`**: Customiza a paginação global (padrão de 30 itens, limite máximo de 100 itens) e registra conversores case-insensitive para os enums de domínio (`StatusLivro`, `StatusConta`, `StatusAluguel` e `PerfilUsuario`).

### 2.2. `entities` — Entidades Imutáveis de Domínio
- **`Genero.java`**: Record imutável representando categorias literárias (`id`, `nome`).
- **`Livro.java`**: Record imutável representando as obras do acervo (`id`, `generosId`, `titulo`, `autor`, `descricao`, `anoPublicacao`, `isbn`, `quantidadeDisponivel`, `statusLivro`).
- **`Usuario.java`**: Record imutável para usuários do sistema (`id`, `nome`, `email`, `@JsonIgnore senha`, `perfil`, `statusConta`).
- **`Aluguel.java`**: Record imutável do cabeçalho de empréstimos (`id`, `usuariosId`, `dataAluguel`, `dataDevolucaoPrevista`, `status`).
- **`AluguelCompleto.java`**: Agregado de domínio que reúne o cabeçalho `Aluguel` e a lista de entidades `Livro` vinculadas ao empréstimo.
- **`DetalheAluguel.java`**: Record imutável associativo da relação N:N entre empréstimos e livros (`id`, `alugueisId`, `livrosId`).
- **`PerfilUsuario.java`**: Enumeração com os perfis de acesso do sistema (`admin`, `cliente`).
- **`StatusLivro.java`**: Enumeração da situação do exemplar (`ativo`, `desativado`).
- **`StatusConta.java`**: Enumeração da situação cadastral do usuário (`ativo`, `suspenso`, `desativado`).
- **`StatusAluguel.java`**: Enumeração do ciclo de vida do empréstimo (`ativo`, `concluido`, `atrasado`).

### 2.3. `dtos` — Objetos de Transferência de Dados
- **`LoginRequestDTO.java`**: Credenciais de entrada para autenticação (`email`, `senha`).
- **`LoginResponseDTO.java`**: Resposta de login autenticado (`tipo: "Bearer"`, `token`, `usuario`).
- **`UsuarioRequestDTO.java`**: Formulário de criação de usuário com validação de formato de e-mail e tamanho mínimo de senha.
- **`UsuarioUpdateDTO.java`**: Formulário de atualização cadastral de perfil, dados ou redefinição de senha opcional.
- **`UsuarioResponseDTO.java`**: Visão pública e segura do usuário (omite permanentemente a senha).
- **`LivroRequestDTO.java`**: Dados para inclusão e edição de livros no acervo.
- **`LivroResponseDTO.java`**: Visão do livro com o nome do gênero embutido para exibição na vitrine.
- **`GeneroRequestDTO.java`**: Formulário para cadastro e atualização de gêneros literários.
- **`GeneroResponseDTO.java`**: Projeção de gêneros para preenchimento de filtros e selects no frontend.
- **`AluguelRequestDTO.java`**: Formulário de locação contendo identificador do usuário, prazo de entrega e lista de IDs de livros.
- **`AluguelResumoResponseDTO.java`**: Projeção resumida de aluguel para tabelas administrativas e histórico do leitor.
- **`AluguelDetalhadoResponseDTO.java`**: Projeção completa agregando os dados do aluguel e a lista detalhada dos livros emprestados.
- **`AluguelUpdateDTO.java`**: Atualização de prazo de entrega ou status do empréstimo.
- **`AluguelCancelamentoDTO.java`**: Justificativa opcional para cancelamento de empréstimo.
- **`ErrorResponseDTO.java`**: Formato padrão de erro da API (`status`, `erros`, `horario`).

### 2.4. `repositories` — Acesso a Dados com JdbcTemplate
- **`UsuarioRepository.java`**: Consultas parametrizadas na tabela `usuarios`, persistência de perfil, soft-delete e consulta restrita `findByEmailComSenha` para autenticação.
- **`LivroRepository.java`**: Operações na tabela `livros`, filtros dinâmicos de busca, decremento atômico condicional de estoque (`quantidade_disponivel > 0`) e incremento em devoluções.
- **`GeneroRepository.java`**: CRUD e validações de unicidade na tabela `generos`.
- **`AluguelRepository.java`**: Operações nas tabelas `alugueis` e `detalhes_alugueis`, agregações com INNER JOIN e consultas com paginação otimizada.

### 2.5. `services` — Regras de Negócio e Transações
- **`AuthService.java`**: Validação de credenciais de login via `PasswordEncoder.matches`, bloqueio de contas suspensas/desativadas e emissão de tokens JWT.
- **`UsuarioService.java`**: Criação de leitores, restrição de criação de perfil admin para administradores, soft-delete e validação de dono nas alterações.
- **`LivroService.java`**: Validação de ISBN único, integridade referencial com gêneros e soft-delete de exemplares.
- **`GeneroService.java`**: Gerenciamento de categorias e validação de duplicidade de nomes.
- **`AluguelService.java`**: Ciclo de vida completo dos empréstimos, validação de livros duplicados na requisição, decremento de estoque, validação de posse do cliente e devolução atômica ao estoque físico.

### 2.6. `controllers` — Controladores REST
- **`AuthController.java`**: Endpoint de autenticação `POST /auth/login`.
- **`UsuarioController.java`**: Endpoints para cadastro, edição, listagem paginada e histórico de aluguéis (`/usuarios`).
- **`LivroController.java`**: Endpoints de catálogo e gerenciamento de acervo (`/livros`).
- **`GeneroController.java`**: Endpoints para consulta e manutenção de categorias (`/generos`).
- **`AluguelController.java`**: Endpoints para empréstimo, devolução, cancelamento e acompanhamento de prazos (`/alugueis`).

### 2.7. `mappers` — Mapeadores de Objetos
- **`UsuarioMapper.java`**: Converte `UsuarioRequestDTO` em `Usuario` e `Usuario` em `UsuarioResponseDTO`.
- **`LivroMapper.java`**: Converte `LivroRequestDTO` na entidade `Livro`.
- **`GeneroMapper.java`**: Converte `GeneroRequestDTO` na entidade `Genero` e projeta `GeneroResponseDTO`.
- **`AluguelMapper.java`**: Transforma dados de locação nas entidades `Aluguel` e `DetalheAluguel` e converte `AluguelCompleto` em DTO detalhado para os Controllers responderem.

### 2.8. `security` — Camada de Autenticação e Autorização JWT
- **`JwtService.java`**: Gera, decodifica e valida tokens HMAC-SHA256, contendo subject (e-mail) e claims de id, nome e perfil.
- **`JwtAuthenticationFilter.java`**: Filtro que intercepta requisições HTTP, extrai o token Bearer e preenche o contexto de segurança com `ROLE_ADMIN` ou `ROLE_CLIENTE`.
- **`SecurityUtils.java`**: Componente utilitário que extrai o usuário logado e facilita checagens de propriedade de recursos.
- **`CustomAuthenticationEntryPoint.java`**: Interceptador de falhas de autenticação que responde HTTP 401 padronizado via Jackson 3.
- **`CustomAccessDeniedHandler.java`**: Interceptador de acessos não autorizados que responde HTTP 403 padronizado via Jackson 3.
- **`SecurityConfig.java`**: Central de configuração de segurança declarativa, CORS, CSRF desabilitado, sessão stateless e mapa granular de permissões por rota.

### 2.9. `exceptions` — Tratamento Centralizado de Erros
- **`CustomException.java`**: Exceção de negócio disparada pelos serviços com status HTTP e lista explicativa de erros.
- **`GlobalExceptionHandler.java`**: Captura central `@RestControllerAdvice` convertendo falhas de validação (`@Valid`), JSON inválido, rotas inexistentes, conflitos de banco e erros de segurança em respostas `ErrorResponseDTO`.

---

## 3. Tabela Completa de Rotas da API

| Método | URL | Permissão | Corpo da Requisição (Input) | Resposta Esperada (Output) |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/auth/login` | Público | `LoginRequestDTO` (`email`, `senha`) | `200 OK` + `LoginResponseDTO` (`tipo`, `token`, `usuario`) |
| **POST** | `/usuarios` | Público | `UsuarioRequestDTO` (`nome`, `email`, `senha`, `perfil?`) | `201 Created` + `UsuarioResponseDTO` |
| **GET** | `/usuarios` | **Apenas Admin** | Nenhum (Parâmetros: `page`, `size`) | `200 OK` + `Page<UsuarioResponseDTO>` |
| **GET** | `/usuarios/{id}` | Autenticado *(Dono ou Admin)* | Nenhum | `200 OK` + `UsuarioResponseDTO` |
| **GET** | `/usuarios/{id}/alugueis` | Autenticado *(Dono ou Admin)* | Nenhum (Parâmetros: `page`, `size`) | `200 OK` + `Page<AluguelResumoResponseDTO>` |
| **PUT** | `/usuarios/{id}` | Autenticado *(Dono ou Admin)* | `UsuarioUpdateDTO` (`nome`, `email`, `senha?`, `perfil?`, `statusConta?`) | `200 OK` + `UsuarioResponseDTO` |
| **DELETE** | `/usuarios/{id}` | **Apenas Admin** | Nenhum | `204 No Content` |
| **GET** | `/generos` | Público | Nenhum (Parâmetros: `page`, `size`) | `200 OK` + `Page<GeneroResponseDTO>` |
| **GET** | `/generos/{id}` | Público | Nenhum | `200 OK` + `GeneroResponseDTO` |
| **POST** | `/generos` | **Apenas Admin** | `GeneroRequestDTO` (`nome`) | `201 Created` + `GeneroResponseDTO` |
| **PUT** | `/generos/{id}` | **Apenas Admin** | `GeneroRequestDTO` (`nome`) | `200 OK` + `GeneroResponseDTO` |
| **DELETE** | `/generos/{id}` | **Apenas Admin** | Nenhum | `204 No Content` |
| **GET** | `/livros` | Público | Nenhum (Filtros: `status`, `generoId`, paginação) | `200 OK` + `Page<LivroResponseDTO>` |
| **GET** | `/livros/{id}` | Público | Nenhum | `200 OK` + `LivroResponseDTO` |
| **POST** | `/livros` | **Apenas Admin** | `LivroRequestDTO` | `201 Created` + `LivroResponseDTO` |
| **PUT** | `/livros/{id}` | **Apenas Admin** | `LivroRequestDTO` | `200 OK` + `LivroResponseDTO` |
| **DELETE** | `/livros/{id}` | **Apenas Admin** | Nenhum | `204 No Content` |
| **GET** | `/alugueis` | **Apenas Admin** | Nenhum (Parâmetros: `page`, `size`) | `200 OK` + `Page<AluguelResumoResponseDTO>` |
| **GET** | `/alugueis/{id}` | Autenticado *(Dono ou Admin)* | Nenhum | `200 OK` + `AluguelDetalhadoResponseDTO` |
| **POST** | `/alugueis` | Autenticado *(Dono ou Admin)* | `AluguelRequestDTO` (`usuarioId`, `dataDevolucaoPrevista`, `livrosIds`) | `201 Created` + `AluguelDetalhadoResponseDTO` |
| **PUT** | `/alugueis/{id}` | **Apenas Admin** | `AluguelUpdateDTO` (`dataDevolucaoPrevista?`, `status?`) | `200 OK` + `AluguelDetalhadoResponseDTO` |
| **POST** | `/alugueis/{id}/devolver` | Autenticado *(Dono ou Admin)* | Nenhum | `200 OK` + `AluguelDetalhadoResponseDTO` |
| **POST** | `/alugueis/{id}/cancelar` | Autenticado *(Dono ou Admin)* | `AluguelCancelamentoDTO` (`motivo?`) | `200 OK` + `AluguelDetalhadoResponseDTO` |

---

## 4. Segurança e Regras de Negócio Críticas

### 4.1. Criptografia e Autenticação JWT
- As senhas dos usuários nunca são persistidas em texto puro; passam obrigatoriamente por hashing unidirecional irreversível com **BCrypt** (`PasswordEncoder`).
- O segredo de assinatura (`jwt.secret`) e o tempo de expiração (`jwt.expiration`) são externalizados via `application.properties` ou variáveis de ambiente.
- O token JWT gerado carrega claims legíveis e seguras (`id`, `nome`, `perfil`) com validade padrão de 24 horas (86.400.000 ms).

### 4.2. Proteção de Dono (Owner Resource Protection)
- Usuários com perfil `cliente` possuem escopo estritamente restrito:
  - Não podem acessar nem alterar contas de outros leitores.
  - Não podem alugar livros em nome de terceiros.
  - Não podem visualizar aluguéis alheios nem registrar devoluções de aluguéis que não lhes pertençam.
  - Não podem alterar seu próprio perfil para `admin` nem reativar contas com status `suspenso` ou `desativado`.
- Administradores (`admin`) têm autoridade total para inspecionar, auditar e gerenciar quaisquer contas e empréstimos.

### 4.3. Controle Concorrente de Estoque Físico
- Ao realizar um aluguel, o acervo de cada livro é decrementado através de um comando atômico condicional no MySQL:
  ```sql
  UPDATE livros SET quantidade_disponivel = quantidade_disponivel - 1 WHERE id = ? AND quantidade_disponivel > 0;
  ```
  Isso previne condições de corrida (*race conditions*) em requisições paralelas, garantindo que o saldo de exemplares nunca fique negativo.
- Em devoluções ou cancelamentos de aluguéis ativos/atrasados, o estoque é restituído de maneira transacional via `UPDATE livros SET quantidade_disponivel = quantidade_disponivel + 1 WHERE id = ?`.
- Um aluguel devolvido ou cancelado somente é atualizado para concluído por uma operação condicional que exige `status <> 'concluido'`; após essa confirmação, o estoque é restituído uma única vez. Repetições da mesma devolução ou cancelamento são rejeitadas antes da nova reposição.

### 4.4. Padronização Global de Erros HTTP
Toda e qualquer recusa operacional, erro de validação ou exceção interna segue rigorosamente a estrutura uniforme em JSON:
```json
{
  "status": 403,
  "erros": [
    "ACESSO NEGADO: VOCÊ SÓ PODE ACESSAR SEUS PRÓPRIOS DADOS"
  ],
  "horario": "2026-11-13T15:45:00"
}
```
Falhas internas imprevistas nunca vazam stack traces para o frontend; geram logs com `log.error` no servidor e entregam código HTTP `500 Internal Server Error`.
