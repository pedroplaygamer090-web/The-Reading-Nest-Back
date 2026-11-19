-- ==============================================================================
-- The Reading Nest — Seed de Dados Iniciais (Sincronizado com o Front-end)
-- Execute este script no phpMyAdmin dentro do banco `the-reading-nest_db`
-- ==============================================================================

USE `the-reading-nest_db`;

-- 1. Categorias e Gêneros Literários
INSERT IGNORE INTO generos (id, nome) VALUES
('c1d2e3f4-0001-4000-8000-000000000001', 'Clássicos'),
('c1d2e3f4-0002-4000-8000-000000000002', 'Romance'),
('c1d2e3f4-0003-4000-8000-000000000003', 'Fantasia'),
('c1d2e3f4-0004-4000-8000-000000000004', 'Ficção');

-- 2. Usuários de Demonstração (Senhas com BCrypt de 10 rounds válidas no Spring Security)
-- Admin:   admin@thereadingnest.com   | Admin123!
-- Cliente: cliente@thereadingnest.com | Cliente123!
INSERT IGNORE INTO usuarios (id, nome, email, senha, perfil, status_conta) VALUES
('u1d2e3f4-0001-4000-8000-000000000001', 'Administrador', 'admin@thereadingnest.com', '$2a$10$Difd0T3pC32O4jKKSM4gCezULfMFf1POXRhJ8ibipKxVIesVjToJa', 'admin', 'ativo'),
('u1d2e3f4-0002-4000-8000-000000000002', 'Cliente Demonstração', 'cliente@thereadingnest.com', '$2a$10$4wThKG1Gb60KVcKuvVOuYuYj6aQks6Tpx.kSbC.42HlIKYZqgQNqi', 'cliente', 'ativo');

-- 3. Acervo com os 8 Livros Oficiais do Front-end
INSERT IGNORE INTO livros (id, generos_id, titulo, autor, descricao, ano_publicacao, isbn, quantidade_disponivel, status_livro) VALUES
('b1d2e3f4-0001-4000-8000-000000000001', 'c1d2e3f4-0001-4000-8000-000000000001', 'Dom Casmurro', 'Machado de Assis', 'Um dos grandes romances da literatura brasileira. Entre lembranças, ciúmes e dúvidas, Bentinho revisita sua história com Capitu e convida o leitor a questionar cada detalhe.', 1899, '9788535910663', 4, 'ativo'),
('b1d2e3f4-0002-4000-8000-000000000002', 'c1d2e3f4-0002-4000-8000-000000000002', 'Orgulho e Preconceito', 'Jane Austen', 'Elizabeth Bennet e Mr. Darcy protagonizam uma história sobre primeiras impressões, orgulho, afeto e as convenções sociais da Inglaterra do século XIX.', 1813, '9788544001820', 3, 'ativo'),
('b1d2e3f4-0003-4000-8000-000000000003', 'c1d2e3f4-0003-4000-8000-000000000003', 'O Pequeno Príncipe', 'Antoine de Saint-Exupéry', 'Uma fábula delicada sobre amizade, afeto e o essencial que tantas vezes passa despercebido aos olhos.', 1943, '9788522031443', 5, 'ativo'),
('b1d2e3f4-0004-4000-8000-000000000004', 'c1d2e3f4-0004-4000-8000-000000000004', '1984', 'George Orwell', 'Uma distopia marcante sobre vigilância, linguagem, liberdade e poder, em uma sociedade controlada pelo Grande Irmão.', 1949, '9788535914845', 2, 'ativo'),
('b1d2e3f4-0005-4000-8000-000000000005', 'c1d2e3f4-0001-4000-8000-000000000001', 'A Hora da Estrela', 'Clarice Lispector', 'A trajetória de Macabéa, jovem nordestina no Rio de Janeiro, narrada com a sensibilidade e a profundidade características de Clarice Lispector.', 1977, '9788532508129', 3, 'ativo'),
('b1d2e3f4-0006-4000-8000-000000000006', 'c1d2e3f4-0003-4000-8000-000000000003', 'O Hobbit', 'J. R. R. Tolkien', 'Bilbo Bolseiro deixa o conforto de sua toca para viver uma aventura extraordinária ao lado de anões e de um mago.', 1937, '9788595084742', 2, 'ativo'),
('b1d2e3f4-0007-4000-8000-000000000007', 'c1d2e3f4-0004-4000-8000-000000000004', 'A Revolução dos Bichos', 'George Orwell', 'Uma fábula satírica sobre poder, manipulação e desigualdade, protagonizada pelos animais de uma fazenda.', 1945, '9788535909551', 4, 'ativo'),
('b1d2e3f4-0008-4000-8000-000000000008', 'c1d2e3f4-0002-4000-8000-000000000002', 'Persuasão', 'Jane Austen', 'Anne Elliot reencontra um amor do passado em um romance sobre amadurecimento, escolhas e segundas oportunidades.', 1817, '9788544002131', 1, 'ativo');

-- 4. Exemplo de Aluguel Ativo para o Cliente de Demonstração
INSERT IGNORE INTO alugueis (id, usuarios_id, data_aluguel, data_devolucao_prevista, status) VALUES
('a1d2e3f4-0001-4000-8000-000000000001', 'u1d2e3f4-0002-4000-8000-000000000002', NOW(), DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'ativo');

INSERT IGNORE INTO detalhes_alugueis (id, alugueis_id, livros_id) VALUES
('d1d2e3f4-0001-4000-8000-000000000001', 'a1d2e3f4-0001-4000-8000-000000000001', 'b1d2e3f4-0001-4000-8000-000000000001');
