# Relatório do Backend — The Reading Nest

**Revisão em:** 25/11/2026
**Escopo:** arquitetura, DTOs e validações, mappers, controllers, regras principais, segurança, persistência e testes.

## Conclusão da revisão

O backend tem uma base coerente em camadas e os mappers realmente fazem as conversões entre entidades e DTOs. Os controllers **não instanciam DTOs diretamente**: recebem DTOs de entrada, delegam a operação ao service e usam o mapper injetado para converter entidades em DTOs de resposta. Isso é diferente de escrever `new AlgumResponseDTO(...)` dentro do controller.

Nem todas as validações cobrem os casos possíveis e há regras do ciclo de aluguel que precisam de decisão/correção antes de descrever o comportamento como totalmente seguro. Os principais pontos encontrados:

1. `AluguelRequestDTO.livrosIds` rejeita lista vazia, mas não rejeita identificadores nulos ou em branco dentro da lista.
2. `AluguelUpdateDTO` não valida se a nova data é futura nem exige que ao menos um campo seja informado.
3. `UsuarioUpdateDTO.statusConta` é `String` sem validação no DTO; os valores são verificados manualmente no service e apenas em minúsculas.
4. As validações de tamanho de `GeneroRequestDTO.nome` (60) e `LivroRequestDTO.autor` (100) são mais restritivas que o banco (100 e 150, respectivamente). Isso pode ser intencional, mas não é um limite idêntico ao DDL.
5. Alterar um aluguel para `concluido` por `PUT /alugueis/{id}` repõe o estoque sem a mesma atualização condicional usada na devolução/cancelamento; chamadas concorrentes podem repor o estoque mais de uma vez. Também é possível alterar o status de um aluguel concluído sem ajuste inverso de estoque.
6. O cancelamento não tem status próprio no enum/schema: `POST /alugueis/{id}/cancelar` marca o aluguel como `concluido` e registra o motivo somente no log.
7. A configuração JWT foi ajustada para exigir `JWT_SECRET` externamente, sem chave padrão no código. Cada ambiente, incluindo o Render, ainda precisa ter uma chave forte configurada; a aplicação não inicia se ela estiver ausente.
8. CORS aceita qualquer origem (`allowedOriginPatterns("*")`) junto com credenciais. Para produção, a origem deve ser restringida aos domínios legítimos do frontend.

Esses pontos são ressalvas identificadas na revisão; não foram alteradas regras de negócio nesta atualização documental.

## Organização e responsabilidade das camadas

O projeto contém 56 classes Java em nove pacotes principais:

| Camada | Responsabilidade |
|---|---|
| `controllers` | Define rotas HTTP, recebe parâmetros/corpos, aplica `@Valid`, delega ao service e monta a resposta HTTP. |
| `services` | Regras de negócio, autorização por proprietário, geração de UUID, criptografia e transações. |
| `repositories` | Acesso MySQL via `JdbcTemplate`, SQL parametrizado, colunas explícitas e `RowMapper`. |
| `entities` | Records e enums que representam o domínio e os dados persistidos. |
| `dtos` | Contratos de entrada/saída da API, separados das entidades. |
| `mappers` | Conversões entre DTOs e entidades; os mappers de resposta criam os DTOs de saída. |
| `security` | Autenticação JWT, regras de autorização e proteção das rotas. |
| `exceptions` | Exceções de domínio e respostas padronizadas de erro. |
| `config` | CORS, conversão de enums, paginação e serialização de páginas. |

### Fluxo de dados

```text
Requisição HTTP/JSON
  -> Controller (@Valid no DTO de entrada)
  -> Service (regra de negócio e transação)
  -> Repository (JdbcTemplate e MySQL)
  -> Entity/record
  -> Service
  -> Mapper (Entity -> DTO de resposta)
  -> Controller (status/cabeçalhos HTTP)
  -> Resposta JSON
```

O controller chama o mapper para a conversão, mas a construção dos DTOs é feita pelos métodos dos mappers, como `toResponseDTO`, `toResumoDTO` e `toDetalhadoDTO`. `AuthService` monta `LoginResponseDTO`, e o tratamento de erros monta `ErrorResponseDTO`; os controllers não constroem DTOs com `new`.

## DTOs, validação e mapeamento

As validações anotadas nos DTOs são executadas quando o endpoint usa `@Valid`. O `GlobalExceptionHandler` converte violações em `ErrorResponseDTO` com HTTP 400.

| DTO | O que valida | Observação |
|---|---|---|
| `LoginRequestDTO` | E-mail não vazio e com formato aceito; senha obrigatória. | Não limita o tamanho dos campos. |
| `UsuarioRequestDTO` | Nome/e-mail obrigatórios, e-mail, limites de nome/e-mail, senha mínima de 6 caracteres. | Perfil é opcional; a regra de quem pode criar admin é aplicada no service. |
| `UsuarioUpdateDTO` | Nome/e-mail obrigatórios, e-mail e tamanho mínimo da senha quando fornecida. | `statusConta` é texto livre no DTO e validado manualmente no service. |
| `GeneroRequestDTO` | Nome obrigatório e até 60 caracteres. | O banco permite 100 caracteres. |
| `LivroRequestDTO` | Gênero, título e autor obrigatórios; limites de título/autor/ISBN; estoque não negativo. | Ano é opcional (a coluna permite `NULL`); `@Min` não rejeita nulo. Descrição não tem limite no DTO e é `TEXT` no banco. |
| `AluguelRequestDTO` | Usuário obrigatório, data obrigatória e futura, lista de livros não vazia. | Faltam restrições para cada elemento da lista. |
| `AluguelUpdateDTO` | Nenhuma anotação de validação. | Pode aceitar objeto sem alterações e data passada. |
| `AluguelCancelamentoDTO` | Nenhuma validação. | Motivo é opcional e não tem limite de tamanho. |

As restrições de negócio que dependem do estado do banco — por exemplo, se o gênero existe, se o e-mail/ISBN já está cadastrado, se a conta está ativa e se há estoque — ficam nos services, como devem. A validação de DTO não substitui essas verificações.

### Mapeamentos verificados

- `LivroMapper`: `LivroRequestDTO -> Livro`; `Livro -> LivroResponseDTO`.
- `GeneroMapper`: `GeneroRequestDTO -> Genero`; `Genero -> GeneroResponseDTO`.
- `UsuarioMapper`: `UsuarioRequestDTO -> Usuario` com senha já criptografada e `Usuario -> UsuarioResponseDTO` sem senha.
- `AluguelMapper`: requisição para `Aluguel` e `DetalheAluguel`; aluguel para DTO resumido; agregado `AluguelCompleto` para DTO detalhado, com livros convertidos via `LivroMapper`.
- Respostas de login usam `UsuarioMapper` para incluir dados públicos do usuário.

Os DTOs de resposta de usuário não têm campo de senha, e as consultas de listagem/perfil não devem expor a senha. A busca com senha existe para autenticação interna.

## Funcionalidades e endpoints

| Método e rota | Acesso configurado | Função |
|---|---|---|
| `POST /auth/login` | Público | Autentica e retorna token e dados públicos do usuário. |
| `POST /usuarios` | Público | Cadastra usuário; o perfil administrador exige autorização no service. |
| `GET /usuarios` | Admin | Lista usuários paginados. |
| `GET /usuarios/{id}` | Autenticado | Consulta próprio perfil ou, conforme regra do service, perfil autorizado. |
| `PUT /usuarios/{id}` | Autenticado | Atualiza cadastro conforme regras de perfil e propriedade. |
| `DELETE /usuarios/{id}` | Admin | Desativa conta. |
| `GET /usuarios/{id}/alugueis` | Autenticado | Consulta histórico paginado, com verificação de propriedade no service. |
| `GET /generos`, `GET /generos/{id}` | Público | Consulta gêneros. |
| `POST /generos`, `PUT /generos/{id}`, `DELETE /generos/{id}` | Admin | Gerencia gêneros. |
| `GET /livros`, `GET /livros/{id}` | Público | Consulta catálogo, filtros e detalhes. |
| `POST /livros`, `PUT /livros/{id}`, `DELETE /livros/{id}` | Admin | Gerencia acervo; a exclusão é desativação lógica. |
| `GET /alugueis` | Admin | Lista empréstimos não concluídos. |
| `GET /alugueis/meus` | Autenticado | Lista histórico do usuário autenticado. |
| `GET /alugueis/{id}` | Autenticado | Consulta empréstimo após verificação de propriedade. |
| `POST /alugueis` | Autenticado | Cria aluguel e reserva estoque. |
| `PUT /alugueis/{id}` | Admin | Atualiza prazo/status. |
| `POST /alugueis/{id}/devolver` | Autenticado | Conclui o aluguel e repõe estoque. |
| `POST /alugueis/{id}/cancelar` | Autenticado | Hoje também conclui o aluguel; motivo é registrado no log. |

Todas as rotas paginadas usam `Pageable`: padrão de 30 itens e máximo de 100. As queries de listagem do projeto usam `LIMIT`/`OFFSET` e uma consulta de contagem.

## Persistência e modelo de dados

O banco MySQL possui as tabelas `usuarios`, `generos`, `livros`, `alugueis` e `detalhes_alugueis`. Os IDs são strings UUID geradas na aplicação. As entidades Java são records imutáveis e os enums de domínio usam valores minúsculos.

Os repositories usam `JdbcTemplate`, parâmetros `?`, nomes explícitos de colunas e `RowMapper`; não foi encontrado `SELECT *` nos repositórios revisados. A criação de aluguel usa decremento condicional de estoque para impedir saldo negativo e agrupa as operações transacionais.

**Diferença relevante:** o ISBN é verificado pelo service, mas `schema.sql` não declara índice `UNIQUE` para `livros.isbn`. Portanto, duas inserções concorrentes ainda podem cadastrar o mesmo ISBN; a restrição no banco é necessária se a unicidade precisar ser garantida sob concorrência.

## Autenticação, segurança e erros

- Senhas são armazenadas como hash BCrypt e comparadas com `PasswordEncoder.matches`.
- JWT é emitido após login e enviado pelo cliente como token Bearer. O filtro verifica assinatura/expiração, consulta o usuário e só autentica contas ativas.
- A aplicação usa sessões stateless e associa perfis às autoridades `ROLE_ADMIN` e `ROLE_CLIENTE`.
- As regras HTTP são configuradas no `SecurityConfig`; a propriedade do recurso também é validada nos services.
- `GlobalExceptionHandler` padroniza erros com `status`, `erros` e `horario`; exceções inesperadas são registradas no log.
- `JWT_SECRET` é obrigatória e fornecida externamente, sem valor padrão no código; confirme que está configurada como segredo no Render. CORS ainda aceita qualquer origem e deve ser limitado aos domínios autorizados antes de publicar o backend.

## Testes executados

`.\mvnw.cmd test` — **BUILD SUCCESS**, 7 testes executados, 0 falhas e 0 erros. A suíte cobre configuração de paginação, serialização de páginas, regras de histórico de aluguel e carregamento da aplicação. Não há, no conjunto atual, testes dedicados para validação de todos os DTOs, conversões dos mappers, endpoints de autorização ou SQL dos repositories.

## Próximos ajustes recomendados

1. Adicionar validações de elementos em `livrosIds`; validar data e campos de `AluguelUpdateDTO`; validar `statusConta` no próprio DTO.
2. Definir o comportamento esperado de cancelamento (status cancelado ou conclusão) e centralizar a reposição de estoque em transação condicional também para a atualização administrativa.
3. Criar restrição única para ISBN se a unicidade for requisito, alinhando service e schema.
4. Configurar `JWT_SECRET` forte em cada ambiente de execução, incluindo Render; restringir CORS aos endereços autorizados.
5. Aumentar a cobertura automatizada de DTOs, mappers, segurança e repositories.
