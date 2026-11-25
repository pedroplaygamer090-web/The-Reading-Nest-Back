Arquivo de estado da sessao 07

## O que foi implementado

- Diagnostico do HTTP 500 em `POST /auth/login`: o MariaDB local do XAMPP estava parado e o unico schema do projeto existente tinha um espaco no inicio do nome (` the-reading-nest_db`), diferente do nome configurado (`the-reading-nest_db`).
- Iniciada a instancia local do MariaDB e criado o schema com o nome esperado, aplicando `src/main/resources/schema.sql`.
- O schema antigo foi mantido intacto. A nova base esta vazia: nao havia contas no schema antigo nem no schema correto.

## Decisao

- Criar o schema esperado e manter o banco com nome incorreto como estava evita alterar ou apagar os arquivos de dados existentes. O aplicativo ja aponta para o nome correto.

## Testes

- Antes da correcao, `POST /auth/login` retornava HTTP 500 mesmo com credenciais deliberadamente inexistentes; a API nao conseguia executar a consulta.
- Depois da correcao, a mesma requisicao retorna HTTP 401 de credenciais invalidas, indicando que o banco e a consulta de login estao acessiveis.
- Cadastro temporario seguido de login retornou HTTP 201 e login bem-sucedido; o usuario temporario foi removido.
- `mvnw.cmd test` com Java 25.0.4: BUILD SUCCESS, 3 testes, sem falhas.

## Observacao importante

- Como nao havia contas no banco local, contas antigas nao existem na base corrigida. O usuario precisa cadastrar a conta novamente ou importar/semear os usuarios corretos. As tabelas estao criadas, mas a base nao foi populada com dados de demonstracao.
- O MariaDB do XAMPP precisa continuar em execucao para o backend conectar.
