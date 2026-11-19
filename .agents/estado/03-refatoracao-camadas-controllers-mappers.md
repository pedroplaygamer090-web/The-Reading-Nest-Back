# Registro de Estado - Sessão 03: Refatoração de Camadas (Controllers, Services, Repositories e Mappers)

## O que foi implementado

### 1. Camada de Configuração (WebConfig e application.properties)
- Removido o registro manual de `PageableHandlerMethodArgumentResolver` em `WebConfig.java`.
- A paginação padrão global (tamanho padrão 30, limite máximo 100) agora é gerenciada declarativamente pelo Spring Boot através de `spring.data.web.pageable.default-page-size=30` e `spring.data.web.pageable.max-page-size=100` em `application.properties`.
- Mantidos em `WebConfig.java` os conversores case-insensitive para os enumeradores de domínio (`StatusLivro`, `StatusConta`, `StatusAluguel`, `PerfilUsuario`).

### 2. Camada de Entidades (Entities)
- `Livro.java`: Adicionado campo `generoNome` (com construtor de conveniência para criação a partir de dados puros da tabela sem join) para transportar o nome do gênero obtido na junção relacional sem depender de DTO no repositório.
- `Aluguel.java`: Adicionado campo `usuarioNome` (com construtor de conveniência) para transporte do nome do usuário obtido via join.
- `AluguelCompleto.java`: Criado record composto agregador em `entities` contendo o cabeçalho `Aluguel` e a lista `List<Livro>` associada.

### 3. Camada de Repositórios (Repositories)
- Eliminados todos os `RowMapper<*ResponseDTO>` dos quatro repositórios (`GeneroRepository`, `LivroRepository`, `UsuarioRepository`, `AluguelRepository`).
- Os repositórios agora trabalham e retornam exclusivamente **Entidades puras** e páginas de entidades (`Genero`, `Livro`, `Usuario`, `Aluguel`, `AluguelCompleto`).
- Preservada a regra de segurança que impede a projeção da coluna `senha` nas listagens e buscas comuns de usuários.

### 4. Camada de Serviços (Services)
- `GeneroService`, `LivroService`, `UsuarioService` e `AluguelService` foram desacoplados de `*ResponseDTO`.
- Todos os métodos de negócio agora retornam entidades de domínio puras (`Genero`, `Livro`, `Usuario`, `AluguelCompleto`, `Page<Aluguel>`).
- Mantida a orquestração de integridade de dados, transações `@Transactional`, hashing de senhas e controle de concorrência com decremento/incremento atômico de estoque.

### 5. Camada de Mappers (Mappers)
- `GeneroMapper`: Mapeia bidirecionalmente `GeneroRequestDTO` <-> `Genero` <-> `GeneroResponseDTO`.
- `LivroMapper`: Mapeia `LivroRequestDTO` <-> `Livro` <-> `LivroResponseDTO`.
- `UsuarioMapper`: Mapeia `UsuarioRequestDTO` <-> `Usuario` <-> `UsuarioResponseDTO`.
- `AluguelMapper`: Injeta `LivroMapper` e mapeia `AluguelRequestDTO` <-> `Aluguel`, `Aluguel` -> `AluguelResumoResponseDTO` e `AluguelCompleto` -> `AluguelDetalhadoResponseDTO`.

### 6. Camada de Controladores (Controllers)
- `GeneroController`, `LivroController`, `UsuarioController` e `AluguelController` agora injetam os respectivos Mappers.
- Fluxo de trabalho padronizado:
  1. O Controller recebe e valida a requisição HTTP.
  2. Delega para o Service, que executa as regras e devolve a Entidade de domínio.
  3. O Controller chama explicitamente o `Mapper` para transformar a Entidade em `ResponseDTO`.
  4. Responde o DTO envolvido em `ResponseEntity`.

---

## Decisões Arquiteturais e Motivos

- **Desacoplamento do Repositório em relação a DTOs:** Repositórios nunca devem conhecer contratos de transporte da camada web (DTOs). Projeções SQL agora alimentam entidades de domínio com atributos de junção explícitos.
- **Mapper na borda (Controller):** O Service representa a lógica de negócio pura do sistema e não deve ser formatador de respostas para o cliente HTTP. A responsabilidade de projetar a Entidade em DTO de saída pertence ao Controller utilizando o Mapper.
- **Paginação Declarativa:** Configurar `Pageable` diretamente no `application.properties` evita boilerplate e conflitos com a auto-configuração do Spring Boot.

---

## O que foi testado e resultado

- Compilação completa do projeto com o Maven Wrapper (`.\mvnw.cmd test-compile`):
  - 54 arquivos fonte recompilados com sucesso.
  - Zero erros e zero avisos de compilação.
  - Testes de contexto do Spring Boot validados.

---

## O que ficou fora do escopo (próximos passos)

- Testes de integração end-to-end com banco de dados MySQL de teste para validar o ciclo completo de aluguel e autenticação.
- Documentação OpenAPI/Swagger caso solicitada para visualização gráfica dos contratos da API.
