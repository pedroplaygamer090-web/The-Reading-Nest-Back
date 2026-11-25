package com.br.The_Reading_Nest_Back;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminFixRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public AdminFixRunner(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminHash = passwordEncoder.encode("Admin123!");
        String clienteHash = passwordEncoder.encode("Cliente123!");

        jdbcTemplate.update(
                "INSERT INTO usuarios (id, nome, email, senha, versao_senha, perfil, status_conta) " +
                        "VALUES ('u1d2e3f4-0001-4000-8000-000000000001', 'Administrador', 'admin@thereadingnest.com', ?, 0, 'admin', 'ativo') " +
                        "ON DUPLICATE KEY UPDATE senha = VALUES(senha), perfil = 'admin', status_conta = 'ativo'",
                adminHash
        );

        jdbcTemplate.update(
                "INSERT INTO usuarios (id, nome, email, senha, versao_senha, perfil, status_conta) " +
                        "VALUES ('u1d2e3f4-0002-4000-8000-000000000002', 'Cliente Demonstração', 'cliente@thereadingnest.com', ?, 0, 'cliente', 'ativo') " +
                        "ON DUPLICATE KEY UPDATE senha = VALUES(senha), perfil = 'cliente', status_conta = 'ativo'",
                clienteHash
        );
    }
}
