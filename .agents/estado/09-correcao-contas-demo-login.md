Arquivo de estado da sessao 09

## Diagnostico

- A aplicacao aponta para o schema `the-reading-nest_db`.
- O unico schema Reading Nest anterior no MariaDB local chamava-se ` the-reading-nest_db`, com espaco no inicio; nele havia 4 generos e 8 livros, mas nenhuma conta ou aluguel.
- A propriedade `spring.sql.init.mode=never` impede que `seed.sql` seja aplicado automaticamente. Assim, as contas demonstrativas nao existiam.

## Correcao aplicada no MariaDB local

- Criado `the-reading-nest_db` com o nome esperado, preservando sem alteracoes o schema antigo com espaco.
- Aplicados `schema.sql` e `seed.sql` no banco correto.
- Confirmados os dois usuarios de demonstracao ativos, 8 livros e 1 aluguel inicial do cliente.
- O schema antigo permanece intacto e continua contendo os 8 livros originais; nenhum registro foi removido.

## Validacao

- Login de `admin@thereadingnest.com` com a senha de demonstracao `Admin123!`: sucesso, perfil `admin`.
- Login de `cliente@thereadingnest.com` com a senha de demonstracao `Cliente123!`: sucesso, perfil `cliente`.
- `GET /alugueis/meus` autenticado como cliente retornou 1 aluguel.
- `mvnw.cmd test` com Java 25.0.4: BUILD SUCCESS, 7 testes sem falhas.

## Observacao

- A carga foi feita no MariaDB local. Outros ambientes/bancos precisam importar `schema.sql` e `seed.sql` no schema `the-reading-nest_db`.
- Manter o MariaDB do XAMPP ativo para o backend local.
