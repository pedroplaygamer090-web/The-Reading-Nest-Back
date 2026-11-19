Arquivo de estado da sessao 04

## O que foi auditado

- Schema SQL oficial em src/main/resources/schema.sql.
- Configuracoes de seguranca, JWT, CORS, paginacao e excecoes.
- Controllers, services e repositories relacionados a usuarios, livros e alugueis.
- Testes existentes e compilacao da aplicacao.

## Problemas encontrados

- Segredo JWT fixo como fallback em JwtService.java e application.properties.
- Tokens continuam validos apos alteracao de senha ou desativacao por status da conta apos novo login.
- Atualizacoes concorrentes para devolucao ou cancelamento podem repor o estoque mais de uma vez.
- A carga inicial do schema usa CREATE TABLE IF NOT EXISTS, mas nao faz migracao de bancos ja existentes.
- spring.sql.init.mode=never impede a aplicacao automatica do schema pela propria aplicacao.
- Nao existem indices em detalhes_alugueis para alugueis_id e livros_id nem CHECK de quantidade nao negativa.
- O projeto possui apenas um teste de carregamento de contexto; nao cobre regras de aluguel, estoque, autorizacao ou seguranca.

## Testes

- mvnw.cmd test concluido com BUILD SUCCESS.
- 1 teste executado, 0 falhas, 0 erros e 0 ignorados.

## Fora do escopo

- Nenhuma alteracao de codigo foi feita nesta auditoria.
