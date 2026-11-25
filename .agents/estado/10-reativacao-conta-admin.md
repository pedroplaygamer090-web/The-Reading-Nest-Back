# Sessão 10 - Reativação de contas por administrador

## Implementado

### DTO
- `UsuarioUpdateDTO.statusConta` passou a ser `String`, mantendo o campo opcional e sem validação `@NotBlank`.

### Serviço
- O método `UsuarioService.atualizar` converte os valores `ativo`, `suspenso` e `desativado` em `StatusConta`.
- Administradores podem consultar e atualizar usuários desativados.
- Clientes continuam impedidos de alterar perfil para `admin` ou alterar a situação da conta.
- Valores inválidos de `statusConta` enviados por administrador retornam HTTP 400.

### Repositório
- O método `UsuarioRepository.update` passou a controlar separadamente a inclusão de `status_conta` no SQL.
- Foi mantida uma sobrecarga compatível com duas opções para preservar chamadas existentes.

## Decisões
- A conversão da situação da conta foi feita no serviço para validar o domínio e manter o repositório responsável apenas por persistência.
- Nenhuma alteração foi feita em perfis, rotas, CORS, Spring Security ou `application.properties`.

## Validação
- `.\mvnw.cmd clean compile`: `BUILD SUCCESS`.

## Fora do escopo
- Não foram alteradas regras de login ou bloqueio de contas inativas.
