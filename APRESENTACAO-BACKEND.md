# Apresentação do Backend — The Reading Nest

Este resumo foi preparado para explicar o backend em um trabalho. O projeto é uma API REST para administrar livros, gêneros, usuários e empréstimos de uma biblioteca.

## Resumo para falar na apresentação

> “O backend do The Reading Nest foi desenvolvido em Java 25 com Spring Boot 4.1.2. Ele fornece uma API REST para cadastro e consulta de livros e gêneros, gerenciamento de usuários e controle de empréstimos. A aplicação é organizada em camadas: os controllers recebem as requisições, os services aplicam as regras de negócio, os repositories acessam o MySQL com JdbcTemplate, e os mappers convertem entidades em DTOs para a API. A autenticação usa JWT Bearer e as senhas são armazenadas com hash BCrypt. As listagens são paginadas e os erros têm uma estrutura JSON padronizada.”

## Como o backend funciona

Exemplo: um leitor solicita um empréstimo.

1. O frontend envia uma requisição HTTP `POST /alugueis` com os dados do usuário, a data de devolução e os IDs dos livros.
2. O `AluguelController` recebe o JSON como `AluguelRequestDTO`. O `@Valid` solicita a validação das regras anotadas no DTO.
3. O `AluguelService` aplica as regras: verifica se o usuário está ativo, se pode agir em nome desse usuário, se os livros existem e estão disponíveis, e se a lista não contém duplicatas.
4. O service chama os repositories. Eles usam `JdbcTemplate` para executar SQL parametrizado no MySQL e atualizar o estoque dentro de uma transação.
5. Os dados do banco são convertidos em entidades Java pelo `RowMapper`.
6. O `AluguelMapper` converte o aluguel e os livros em `AluguelDetalhadoResponseDTO`.
7. O controller devolve a resposta JSON com HTTP `201 Created`.

```text
Frontend -> Controller -> Service -> Repository -> MySQL
Frontend <- Controller <- Mapper <- Entity <- RowMapper
```

## O que significa cada termo

### API REST

É uma forma de disponibilizar funções do sistema por HTTP. Cada rota representa um recurso; por exemplo, `/livros` representa o catálogo. Verbos como `GET`, `POST`, `PUT` e `DELETE` indicam consulta, criação, atualização e remoção/desativação.

### Controller

É a porta de entrada HTTP. Define as rotas, lê parâmetros e corpos JSON, aplica `@Valid` e chama o service. Não deve conter as regras de negócio nem montar DTOs campo por campo.

Neste projeto, os controllers **não usam `new ...DTO(...)`**. Para respostas que partem de entidades, chamam um mapper, que é quem executa a conversão. Assim, o controller coordena a resposta, mas não implementa o mapeamento manual.

### DTO

DTO significa *Data Transfer Object* (objeto de transferência de dados). É o formato usado para receber ou enviar dados pela API. Por exemplo:

- `LivroRequestDTO`: campos permitidos para cadastrar ou atualizar um livro.
- `LivroResponseDTO`: campos apresentados ao frontend, incluindo o nome do gênero.
- `UsuarioResponseDTO`: dados públicos do usuário, sem a senha.

Separar DTOs das entidades evita expor diretamente a estrutura interna do banco e permite controlar quais campos entram e saem da API.

### Validação de DTO

Anotações como `@NotBlank`, `@Email`, `@Size`, `@Min` e `@Future` descrevem restrições sobre os dados recebidos. Quando o controller usa `@Valid`, o Spring executa essas verificações. Se falharem, o `GlobalExceptionHandler` responde com HTTP 400 e o JSON padronizado.

Validação de formato não substitui regra de negócio: `@NotBlank` pode verificar que o título foi enviado, mas só o service/repository consegue confirmar que o gênero informado existe no banco.

### Mapper / mapeamento

Um mapper é um componente que traduz um tipo de objeto para outro sem misturar as responsabilidades:

- **DTO de entrada -> entidade:** `LivroMapper.toEntity(...)`.
- **Entidade -> DTO de saída:** `LivroMapper.toResponseDTO(...)`.
- **Aluguel completo -> resposta detalhada:** `AluguelMapper.toDetalhadoDTO(...)`.

No projeto, os mappers também aplicam pequenas normalizações, como remover espaços externos de alguns campos. Eles não acessam o banco nem decidem permissões.

### Service

É a camada de regras de negócio. Decide, por exemplo, se uma conta pode alugar, se o usuário é dono do recurso, se há estoque, se um e-mail já existe e como atualizar o empréstimo. Também coordena transações com `@Transactional`.

### Repository e JdbcTemplate

O repository é a camada que conversa com o banco. `JdbcTemplate` executa SQL diretamente; os valores enviados pelo usuário são passados como parâmetros, em vez de concatenados na query. Os resultados são convertidos pelo `RowMapper`.

Uma transação permite agrupar operações relacionadas para que sejam confirmadas juntas ou revertidas em caso de erro. No aluguel, isso é importante para manter alinhados o registro do empréstimo, os detalhes dos livros e o estoque.

### Entity, record e enum

Uma entidade representa os dados do domínio usados internamente e persistidos no banco. Neste projeto, muitas entidades são `record`s, que são objetos concisos e imutáveis. Enums representam conjuntos fechados de valores, como `StatusAluguel` (`ativo`, `concluido`, `atrasado`).

### JWT

JWT significa *JSON Web Token*. É um token assinado que o servidor entrega depois de autenticar o usuário. Nas próximas requisições, o frontend o envia no cabeçalho:

```http
Authorization: Bearer <token>
```

O token tem três partes codificadas em Base64URL: cabeçalho, conteúdo (*claims*) e assinatura. **Codificação não é criptografia:** quem tiver o token consegue ler o conteúdo, então não se deve colocar senha ou segredo nele. A assinatura permite ao backend verificar se o conteúdo foi alterado e se foi emitido com a chave correta. O token deste projeto inclui identificadores e perfil, tem prazo de expiração e é validado pelo filtro JWT. A conta também é consultada para confirmar que continua ativa.

### BCrypt

BCrypt é usado para gerar um hash de senha. O sistema não precisa guardar a senha original: no login, compara a senha digitada com o hash guardado. Hash é diferente de criptografia reversível; não se “descriptografa” a senha armazenada.

### Autenticação e autorização

- **Autenticação:** confirma quem é o usuário, neste caso pelo login e pelo JWT.
- **Autorização:** decide o que esse usuário pode fazer, conforme o perfil e as regras de propriedade. Por exemplo, certas rotas exigem `ROLE_ADMIN`, e o service confere se o cliente está acessando os próprios dados.

### Paginação e tratamento de erros

As listagens aceitam `Pageable`, com tamanho padrão de 30 registros e limite máximo de 100. Isso evita devolver o catálogo inteiro de uma vez.

O `GlobalExceptionHandler` transforma falhas conhecidas em respostas com `status`, `erros` e `horario`. Erros inesperados são registrados no log do servidor e retornam uma mensagem genérica ao cliente.

## Principais áreas funcionais

- **Usuários:** cadastro, consulta e atualização de perfil, com restrições para operações administrativas.
- **Livros:** catálogo, filtro por status/gênero, cadastro e desativação lógica.
- **Gêneros:** consulta e gerenciamento de categorias literárias.
- **Aluguéis:** criação, consulta paginada, histórico, devolução e cancelamento.
- **Segurança:** login, emissão/validação de JWT, perfis e controle de acesso.

## Tecnologias

- Java 25 e Spring Boot 4.1.2.
- Spring MVC para endpoints REST.
- Jakarta Validation para validação de entrada.
- Spring Security e JWT para autenticação/autorização.
- BCrypt para hash de senhas.
- JdbcTemplate e MySQL para persistência, sem JPA/Hibernate.
- Maven Wrapper para compilar e executar testes sem depender de uma instalação global do Maven.

## Situação da revisão

Os mappers convertem entidades em DTOs e os controllers não constroem DTOs diretamente. A suíte atual passou: **7 testes, sem falhas**. A revisão também identificou pontos para melhorar: validar cada ID na lista de livros, validar a atualização de aluguel, alinhar status de cancelamento e reposição de estoque, impor unicidade do ISBN no banco e restringir a configuração de JWT/CORS para produção. Os detalhes estão em [`RELATORIO-BACKEND.md`](RELATORIO-BACKEND.md).

Para a apresentação, é correto dizer que o projeto está organizado e tem autenticação, validações e testes, mas não afirmar que todas as regras estão totalmente cobertas: os pontos acima ainda precisam ser tratados.
