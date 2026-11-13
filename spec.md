Vamos implementar a aplicação. Preciso que você crie a aplicação da biblioteca **The Reading Nest** com o seguinte
modelo de banco de dados (schema `the-reading-nest_db`):

- `generos`: `id` VARCHAR (36) PK, `nome` VARCHAR (60) UNIQUE NOT NULL
- `livros`: `id` VARCHAR (36) PK, `generos_id` VARCHAR (36) FK, `titulo` VARCHAR (150) NOT NULL, `autor` VARCHAR (100)
  NOT NULL, `descricao` TEXT NULL, `ano_publicacao` INT NULL, `isbn` VARCHAR (20) UNIQUE NULL, `quantidade_disponivel`
  INT NOT NULL DEFAULT 1, `status_livro` ENUM ('ativo', 'desativado') NOT NULL DEFAULT 'ativo'
- `usuarios`: `id` VARCHAR (36) PK, `nome` VARCHAR (100) NOT NULL, `email` VARCHAR (100) UNIQUE NOT NULL, `senha`
  VARCHAR (255) NOT NULL, `status_conta` ENUM ('ativo', 'suspenso', 'desativado') NOT NULL DEFAULT 'ativo'
- `alugueis`: `id` VARCHAR (36) PK, `usuarios_id` VARCHAR (36) FK, `data_aluguel` DATETIME NOT NULL DEFAULT
  CURRENT_TIMESTAMP, `data_devolucao_prevista` DATE NOT NULL, `status` ENUM ('ativo', 'concluido', 'atrasado') NOT NULL
  DEFAULT 'ativo'
- `detalhes_alugueis`: `id` VARCHAR (36) PK, `alugueis_id` VARCHAR (36) FK, `livros_id` VARCHAR (36) FK

Você deve implementar as seguintes rotas:
-----
Gêneros POST /generos -> administrador Essa rota cria gêneros literários no banco de dados GET /generos Essa rota busca
todos os gêneros presentes no banco de dados PUT /generos/:id -> administrador Essa rota edita um gênero literário no
banco de dados DELETE /generos/:id -> administrador Essa rota remove um gênero literário do banco de dados

Livros POST /livros -> administrador Essa rota cria um livro no banco de dados (vinculado a um gênero válido)
GET /livros Essa rota busca todos os livros presentes no banco de dados (permitindo filtrar opcionalmente por status ou
gênero)
GET /livros/:id Essa rota busca um livro específico no banco de dados (retornando todos os detalhes, incluindo
descrição, ano, ISBN e gênero)
PUT /livros/:id -> administrador Essa rota atualiza os dados de um livro no banco de dados DELETE /livros/:id ->
administrador Essa rota desativa (soft-delete) um livro no banco de dados

Usuários POST /usuarios -> público/administrador Essa rota cria um usuário no banco de dados GET /usuarios ->
administrador Essa rota busca uma lista de usuários no banco de dados GET /usuarios/:id -> administrador ou próprio
usuário Essa rota busca dados de um usuário em específico GET /usuarios/:id/alugueis -> administrador ou próprio usuário
Essa rota busca o histórico de aluguéis paginado de um usuário específico (para a tela Minha Conta)
PUT /usuarios/:id -> administrador ou próprio usuário Essa rota altera informações do usuário DELETE /usuarios/:id ->
administrador Essa rota desativa (soft-delete) um usuário no banco de dados

Aluguéis POST /alugueis -> cliente Criação de um novo aluguel de livros (validando usuário ativo e estoque disponível de
cada livro)
GET /alugueis -> administrador Buscar todos os aluguéis NÃO CONCLUÍDOS. Deve ser ordenado do mais atrasado e com data de
devolução prevista mais antiga. Não é necessário retornar todos os livros alugados nessa listagem, para que seja mais
performático. GET /alugueis/:id -> administrador ou próprio usuário Buscar dados de um aluguel em específico. Retornar
todos os livros vinculados ao aluguel. PUT /alugueis/:id -> administrador Alterar o status do aluguel (`ativo`,
`concluido`, `atrasado`) e/ou data de devolução prevista, atualizando o estoque caso seja concluído. POST /alugueis/:
id/devolver -> administrador ou próprio usuário Finalização/devolução do aluguel feita pelo usuário ou pelo
administrador, retornando os livros ao estoque disponível. POST /alugueis/:id/cancelar -> administrador ou próprio
usuário Cancelamento feito pelo usuário ou pelo administrador, retornando os livros ao estoque disponível. Caso tenha
sido cancelado pelo administrador, deve aceitar e registrar o motivo.
----
Todos os campos devem ser validados antes de qualquer chamada. Caso o usuário escreva algo incorreto, deve ser retornado
o seguinte template:

{
"status": STATUS_ERRO,
"erros": [
"USUÁRIO NÃO ENCONTRADO",
"ID DO USUÁRIO INVÁLIDO"
],
"horario": "2026-11-12T21:13:00"
}

Para validação, utilizar anotações com o padrão Spring (`jakarta.validation.constraints`). Utilizar DTO + Entity.

Paginação: Todas as rotas com retorno de mais de um dado devem estar paginadas com padrão de 30 e máximo de 100.

Os campos do banco de dados serão feitos de forma padrão. Tenha em mente que todos os ids serão feitos usando UUID
criados diretamente pelo back end.

Preciso que você me retorne um SQL para que eu possa adicionar no banco de dados as tabelas (`the-reading-nest_db`).
Tenha em mente que, como não adicionaremos o sistema de Login por enquanto, você precisa também adicionar alguns
usuários de exemplo (com senhas criptografadas), além do próprio administrador, alguns gêneros literários, alguns livros
e um aluguel de exemplo. Não precisa adicionar, ao menos por enquanto, autorização ou autenticação, deixe todas as rotas
livres até implementarmos na próxima vez.
