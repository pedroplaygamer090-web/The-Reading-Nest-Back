# Sessão 11 - Configuração de deploy e acesso administrativo

## Diagnóstico

- `spring.datasource.url` apontava sempre para `localhost`, que dentro de um container/plataforma de deploy não é o servidor MySQL provisionado.
- Sem conexão com o banco, a aplicação não inicia e o login administrativo não pode funcionar.
- `schema.sql` já cria os dados iniciais com `INSERT IGNORE`; se já existir conta com o mesmo e-mail, o script não atualiza as credenciais ou o perfil existentes.

## Implementado

- A URL de conexão agora pode ser definida por `SPRING_DATASOURCE_URL`, preservando o endereço local como padrão.
- A aplicação usa `PORT` quando fornecida pela plataforma e mantém `8080` como padrão local.
- `DATABASE.md` documenta as variáveis necessárias para o deploy e o comportamento idempotente do seed.

## Validação

- `git diff --check`: sem erros de whitespace.
- `.\mvnw.cmd test`: não executou; o ambiente tem JDK 23, mas o projeto configura `--release 25` (`release version 25 not supported`).

## Fora do escopo

- Não foram alterados dados existentes no MySQL nem redefinida a senha de nenhuma conta.
- A plataforma de deploy não foi identificada; os valores exatos do host, usuário e senha do banco devem ser obtidos no painel do serviço MySQL.
