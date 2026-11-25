Arquivo de estado da sessao 06

## O que foi implementado

- Adicionada a rota autenticada `GET /alugueis/meus`, que lista o histórico paginado de aluguéis do usuário identificado pelo token.
- O serviço reutiliza a consulta existente por usuário e não depende de um ID informado pelo cliente.
- Atualizados `SecurityConfig` e a documentação da API.

## Decisao arquitetural

- A rota de histórico fica disponível no recurso de aluguéis e resolve o usuário pelo contexto de segurança, enquanto `GET /alugueis` permanece exclusivo do administrador para listar aluguéis não concluídos.

## Testes

- `git diff --check`: sem erros.
- `mvnw.cmd test`: não executou; o ambiente tem Java 23, mas o projeto exige Java 25 (`release version 25 not supported`).

## Fora do escopo

- A falha 500 original não foi reproduzida; não havia log da requisição para identificar uma exceção SQL ou outra causa específica.
