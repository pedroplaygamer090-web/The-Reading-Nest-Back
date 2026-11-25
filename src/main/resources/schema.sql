CREATE TABLE IF NOT EXISTS `usuarios` (
                                          `id` varchar(36) NOT NULL,
    `nome` varchar(100) NOT NULL,
    `email` varchar(100) NOT NULL,
    `senha` varchar(255) NOT NULL,
    `versao_senha` bigint(20) NOT NULL DEFAULT 0,
    `perfil` enum('admin','cliente') NOT NULL DEFAULT 'cliente',
    `status_conta` enum('ativo','suspenso','desativado') NOT NULL DEFAULT 'ativo',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_usuarios_email` (`email`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `usuarios` ADD COLUMN IF NOT EXISTS `versao_senha` bigint(20) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS `generos` (
                                         `id` varchar(36) NOT NULL,
    `nome` varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_generos_nome` (`nome`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `livros` (
                                        `id` varchar(36) NOT NULL,
    `generos_id` varchar(36) NOT NULL,
    `titulo` varchar(150) NOT NULL,
    `autor` varchar(150) NOT NULL,
    `descricao` text DEFAULT NULL,
    `ano_publicacao` int(11) DEFAULT NULL,
    `isbn` varchar(20) DEFAULT NULL,
    `quantidade_disponivel` int(11) NOT NULL DEFAULT 0,
    `status_livro` enum('ativo','desativado') NOT NULL DEFAULT 'ativo',
    PRIMARY KEY (`id`),
    KEY `fk_livros_generos` (`generos_id`),
    CONSTRAINT `fk_livros_generos` FOREIGN KEY (`generos_id`) REFERENCES `generos` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `alugueis` (
                                          `id` varchar(36) NOT NULL,
    `usuarios_id` varchar(36) NOT NULL,
    `data_aluguel` timestamp NOT NULL DEFAULT current_timestamp(),
    `data_devolucao_prevista` date NOT NULL,
    `status` enum('ativo','concluido','atrasado') NOT NULL DEFAULT 'ativo',
    PRIMARY KEY (`id`),
    KEY `fk_alugueis_usuarios` (`usuarios_id`),
    KEY `idx_alugueis_status_devolucao` (`status`,`data_devolucao_prevista`),
    CONSTRAINT `fk_alugueis_usuarios` FOREIGN KEY (`usuarios_id`) REFERENCES `usuarios` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `detalhes_alugueis` (
                                                   `id` varchar(36) NOT NULL,
    `alugueis_id` varchar(36) NOT NULL,
    `livros_id` varchar(36) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_detalhes_alugueis` (`alugueis_id`),
    KEY `fk_detalhes_livros` (`livros_id`),
    CONSTRAINT `fk_detalhes_alugueis` FOREIGN KEY (`alugueis_id`) REFERENCES `alugueis` (`id`),
    CONSTRAINT `fk_detalhes_livros` FOREIGN KEY (`livros_id`) REFERENCES `livros` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO `usuarios` (`id`, `nome`, `email`, `senha`, `versao_senha`, `perfil`, `status_conta`) VALUES
('4debdadc-75a5-4518-b956-2456a5701ab6', 'ttttffdPusd', 'pedropaullito@gmail.com', '$2a$10$ZnObTcq4ZdicEq90/Yn.le5GlDj/cL.rii2gTUEGWeaYP9NQH1Ts2', 0, 'cliente', 'ativo'),
('c5ebe140-0c16-4d64-af99-829290bbe59b', 'Pedro Paulo Souza', 'pedroplaygamer090@gmail.com', '$2a$10$E9gMT5hIivnE3tXDHLJWMuPukp29BC3vbOOPWjEy4cGRQsuvEY.SW', 0, 'cliente', 'ativo'),
('u1d2e3f4-0001-4000-8000-000000000001', 'Administrador', 'admin@thereadingnest.com', '$2a$10$Difd0T3pC32O4jKKSM4gCezULfMFf1POXRhJ8ibipKxVIesVjToJa', 0, 'admin', 'ativo'),
('u1d2e3f4-0002-4000-8000-000000000002', 'Cliente Demonstração', 'cliente@thereadingnest.com', '$2a$10$4wThKG1Gb60KVcKuvVOuYuYj6aQks6Tpx.kSbC.42HlIKYZqgQNqi', 0, 'cliente', 'ativo');

INSERT IGNORE INTO `generos` (`id`, `nome`) VALUES
('c1d2e3f4-0001-4000-8000-000000000001', 'Clássicos'),
('c1d2e3f4-0003-4000-8000-000000000003', 'Fantasia'),
('c1d2e3f4-0004-4000-8000-000000000004', 'Ficção'),
('c1d2e3f4-0002-4000-8000-000000000002', 'Romance');

INSERT IGNORE INTO `livros` (`id`, `generos_id`, `titulo`, `autor`, `descricao`, `ano_publicacao`, `isbn`, `quantidade_disponivel`, `status_livro`) VALUES
('b1d2e3f4-0001-4000-8000-000000000001', 'c1d2e3f4-0001-4000-8000-000000000001', 'Dom Casmurro', 'Machado de Assis', 'Um dos grandes romances da literatura brasileira. Entre lembranças, ciúmes e dúvidas, Bentinho revisita sua história com Capitu e convida o leitor a questionar cada detalhe.', 1899, '9788535910663', 5, 'ativo'),
('b1d2e3f4-0002-4000-8000-000000000002', 'c1d2e3f4-0002-4000-8000-000000000002', 'Orgulho e Preconceito', 'Jane Austen', 'Elizabeth Bennet e Mr. Darcy protagonizam uma história sobre primeiras impressões, orgulho, afeto e as convenções sociais da Inglaterra do século XIX.', 1813, '9788544001820', 3, 'ativo'),
('b1d2e3f4-0003-4000-8000-000000000003', 'c1d2e3f4-0003-4000-8000-000000000003', 'O Pequeno Príncipe', 'Antoine de Saint-Exupéry', 'Uma fábula delicada sobre amizade, afeto e o essencial que tantas vezes passa despercebido aos olhos.', 1943, '9788522031443', 5, 'ativo'),
('b1d2e3f4-0004-4000-8000-000000000004', 'c1d2e3f4-0004-4000-8000-000000000004', '1984', 'George Orwell', 'Uma distopia marcante sobre vigilância, linguagem, liberdade e poder, em uma sociedade controlada pelo Grande Irmão.', 1949, '9788535914845', 2, 'ativo'),
('b1d2e3f4-0005-4000-8000-000000000005', 'c1d2e3f4-0001-4000-8000-000000000001', 'A Hora da Estrela', 'Clarice Lispector', 'A trajetória de Macabéa, jovem nordestina no Rio de Janeiro, narrada com a sensibilidade e a profundidade características de Clarice Lispector.', 1977, '9788532508129', 3, 'ativo'),
('b1d2e3f4-0006-4000-8000-000000000006', 'c1d2e3f4-0003-4000-8000-000000000003', 'O Hobbit', 'J. R. R. Tolkien', 'Bilbo Bolseiro deixa o conforto de sua toca para viver uma aventura extraordinária ao lado de anões e de um mago.', 1937, '9788595084742', 2, 'ativo'),
('b1d2e3f4-0007-4000-8000-000000000007', 'c1d2e3f4-0004-4000-8000-000000000004', 'A Revolução dos Bichos', 'George Orwell', 'Uma fábula satírica sobre poder, manipulação e desigualdade, protagonizada pelos animais de uma fazenda.', 1945, '9788535909551', 4, 'ativo'),
('b1d2e3f4-0008-4000-8000-000000000008', 'c1d2e3f4-0002-4000-8000-000000000002', 'Persuasão', 'Jane Austen', 'Anne Elliot reencontra um amor do passado em um romance sobre amadurecimento, escolhas e segundas oportunidades.', 1817, '9788544002131', 1, 'ativo');

UPDATE `usuarios` SET `perfil` = 'admin', `status_conta` = 'ativo' WHERE `email` IN ('admin@thereadingnest.com', 'admin.final@thereadingnest.com', 'admin@grupo.com');
