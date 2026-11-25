# Configuração do banco de dados

A aplicação usa MySQL com o schema `the-reading-nest_db`. Localmente, a conexão padrão aponta para `localhost:3306`. No deploy, configure as variáveis de ambiente:

- `SPRING_DATASOURCE_URL`: URL JDBC completa do MySQL, incluindo host, porta e schema. Exemplo: `jdbc:mysql://<host>:3306/the-reading-nest_db?serverTimezone=UTC`.
- `SPRING_DATASOURCE_USERNAME`: usuário do banco.
- `SPRING_DATASOURCE_PASSWORD`: senha desse usuário.
- `PORT`: porta HTTP informada pela plataforma (opcional; o padrão é `8080`).
- `JWT_SECRET`: chave secreta forte para assinar tokens JWT; configure uma chave própria no deploy.

O banco/schema precisa existir antes de iniciar a aplicação. Com `spring.sql.init.mode=always`, o Spring executa `schema.sql` no início, criando as tabelas e inserindo os registros iniciais, inclusive a conta administrativa demonstrativa. O `INSERT IGNORE` não altera contas que já existam: se o e-mail administrativo já estiver cadastrado no banco, o seed não redefine sua senha, perfil ou status.

No IntelliJ, informe as variáveis em **Run > Edit Configurations > Environment variables**. No PowerShell, antes de iniciar a aplicação:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/the-reading-nest_db?serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "sua-senha"
.\mvnw.cmd spring-boot:run
```

Não salve a senha no `application.properties` nem no Git.
