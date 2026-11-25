# Sessão 12 — Revisão de DTOs, mappers e documentação

## Implementado

- Revisado o caminho de validação dos DTOs com `@Valid`, os mappers de entrada/saída e o uso dos mappers nos controllers.
- Confirmado que controllers não instanciam DTOs diretamente; as conversões de resposta são feitas por componentes `Mapper`.
- Atualizado `RELATORIO-BACKEND.md` com arquitetura, rotas, validações, responsabilidades, resultados dos testes e ressalvas encontradas.
- Criado `APRESENTACAO-BACKEND.md` com resumo do sistema e explicações de API REST, DTO, mapper, service, repository, JdbcTemplate, JWT, BCrypt, autenticação e autorização.

## Decisões e ressalvas registradas

- Não foram alteradas regras funcionais durante esta tarefa documental.
- O relatório registra validações faltantes/incompletas, diferença entre limites dos DTOs e schema, risco de reposição concorrente de estoque, semântica de cancelamento, falta de unicidade de ISBN no banco e valores amplos de JWT/CORS.
- Os controllers chamam mappers para mapear respostas, mas não constroem DTOs manualmente.

## Validação

- `.\mvnw.cmd test`: BUILD SUCCESS; 7 testes executados, 0 falhas e 0 erros.
- Revisão por leitura dos controllers, DTOs, mappers, services, repositories, configuração de segurança, schema SQL e testes existentes.

## Fora do escopo

- Corrigir as regras de aluguel ou alterar o contrato dos DTOs sem decisão sobre comportamento esperado.
- Adicionar novos testes de validação, segurança ou persistência.
