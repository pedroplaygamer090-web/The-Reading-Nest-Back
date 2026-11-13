# Configuração do banco de dados

A aplicação usa MySQL com o schema `the-reading-nest_db`. A senha é carregada da variável de ambiente `SPRING_DATASOURCE_PASSWORD`; defina-a com a senha do usuário configurado em `spring.datasource.username`.

No IntelliJ, informe a variável em **Run > Edit Configurations > Environment variables**. No PowerShell, antes de iniciar a aplicação:

```powershell
\$env:SPRING_DATASOURCE_PASSWORD = "sua-senha"
.\mvnw.cmd spring-boot:run
```

Não salve a senha no `application.properties` nem no Git.
