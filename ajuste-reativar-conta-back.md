# Tarefa: Permitir que ADMIN Reative Contas Desativadas via `PUT /usuarios/{id}`

## Regras Absolutas (PROIBIÇÕES)
- **NÃO altere** os valores de `perfil` no banco ou nas entidades (`'admin'` e `'cliente'` devem continuar exatamente como estão no `schema.sql` e no `SecurityConfig.java`).
- **NÃO altere** rotas existentes (`/usuarios`, `/alugueis`, `/livros`, `/auth/login`), CORS, Spring Security ou `application.properties`.
- **Estilo de Código:** Mantenha os Javadocs originais das classes e métodos, sem adicionar comentários linha a linha no meio do código.

---

## Ajuste Cirúrgico Necessário
Para que o botão **"Ativar Conta"** no painel administrativo do front-end funcione ao enviar `PUT /usuarios/{id}` com `{ "nome": "...", "email": "...", "statusConta": "ativo" }`:

1. **`UsuarioUpdateDTO.java`:**
    - Adicione o campo opcional `String statusConta` (sem `@NotBlank`, para continuar funcionando quando um cliente atualizar apenas o próprio nome ou e-mail).
2. **`UsuarioService.java` e `UsuarioRepository.java`:**
    - No método `atualizar(String id, UsuarioUpdateDTO dto)`:
        - Garanta que, se o usuário autenticado for `ADMIN`, ele consiga buscar e atualizar também usuários que atualmente estejam com `status_conta = 'desativado'`.
        - Se `dto.statusConta()` vier preenchido (`"ativo"`, `"suspenso"` ou `"desativado"`) e o usuário autenticado for `ADMIN`, atualize também a coluna `status_conta` na tabela `usuarios`.
3. **Validação Final:**
    - Execute `.\mvnw.cmd clean compile` e confirme `BUILD SUCCESS`.
