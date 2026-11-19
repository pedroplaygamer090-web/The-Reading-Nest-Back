-- ==============================================================================
-- Schema Oficial e Idempotente do Banco de Dados the-reading-nest_db
-- Sistema: The Reading Nest — Gerenciador de Biblioteca
-- ==============================================================================

-- 1. Tabela de Categorias e Gêneros Literários
CREATE TABLE IF NOT EXISTS generos (
    id VARCHAR(36) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    CONSTRAINT pk_generos PRIMARY KEY (id),
    CONSTRAINT uk_generos_nome UNIQUE (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Tabela do Acervo de Livros
CREATE TABLE IF NOT EXISTS livros (
    id VARCHAR(36) NOT NULL,
    generos_id VARCHAR(36) NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(150) NOT NULL,
    descricao TEXT,
    ano_publicacao INT,
    isbn VARCHAR(20),
    quantidade_disponivel INT NOT NULL DEFAULT 0,
    status_livro ENUM('ativo', 'desativado') NOT NULL DEFAULT 'ativo',
    CONSTRAINT pk_livros PRIMARY KEY (id),
    CONSTRAINT uk_livros_isbn UNIQUE (isbn),
    CONSTRAINT fk_livros_generos FOREIGN KEY (generos_id) REFERENCES generos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Tabela de Leitores e Funcionários (Usuários)
CREATE TABLE IF NOT EXISTS usuarios (
    id VARCHAR(36) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    perfil ENUM('admin', 'cliente') NOT NULL DEFAULT 'cliente',
    status_conta ENUM('ativo', 'suspenso', 'desativado') NOT NULL DEFAULT 'ativo',
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tabela de Cabeçalho de Empréstimos e Aluguéis
CREATE TABLE IF NOT EXISTS alugueis (
    id VARCHAR(36) NOT NULL,
    usuarios_id VARCHAR(36) NOT NULL,
    data_aluguel TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_devolucao_prevista DATE NOT NULL,
    status ENUM('ativo', 'concluido', 'atrasado' ) NOT NULL DEFAULT 'ativo',
    CONSTRAINT pk_alugueis PRIMARY KEY (id),
    CONSTRAINT fk_alugueis_usuarios FOREIGN KEY (usuarios_id) REFERENCES usuarios (id),
    INDEX idx_alugueis_status_devolucao (status, data_devolucao_prevista)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Tabela Associativa de Itens Alugados
CREATE TABLE IF NOT EXISTS detalhes_alugueis (
    id VARCHAR(36) NOT NULL,
    alugueis_id VARCHAR(36) NOT NULL,
    livros_id VARCHAR(36) NOT NULL,
    CONSTRAINT pk_detalhes_alugueis PRIMARY KEY (id),
    CONSTRAINT fk_detalhes_alugueis_alugueis FOREIGN KEY (alugueis_id) REFERENCES alugueis (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalhes_alugueis_livros FOREIGN KEY (livros_id) REFERENCES livros (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
