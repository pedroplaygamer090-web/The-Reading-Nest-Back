-- Acervo inicial de livros do The Reading Nest.
-- No phpMyAdmin, selecione primeiro o banco the-reading-nest_db e execute este arquivo.
-- O script cria os gêneros ausentes e não duplica livros com ISBN já cadastrado.

START TRANSACTION;

INSERT IGNORE INTO generos (id, nome) VALUES
('c1d2e3f4-0001-4000-8000-000000000001', 'Clássicos'),
('c1d2e3f4-0002-4000-8000-000000000002', 'Romance'),
('c1d2e3f4-0003-4000-8000-000000000003', 'Fantasia'),
('c1d2e3f4-0004-4000-8000-000000000004', 'Ficção');

INSERT IGNORE INTO livros (
    id,
    generos_id,
    titulo,
    autor,
    descricao,
    ano_publicacao,
    isbn,
    quantidade_disponivel,
    status_livro
)
SELECT
    dados.id,
    genero.id,
    dados.titulo,
    dados.autor,
    dados.descricao,
    dados.ano_publicacao,
    dados.isbn,
    dados.quantidade_disponivel,
    'ativo'
FROM (
    SELECT
        'b1d2e3f4-0001-4000-8000-000000000001' AS id,
        'Clássicos' AS genero,
        'Dom Casmurro' AS titulo,
        'Machado de Assis' AS autor,
        'Bentinho relembra sua história com Capitu em um romance sobre memória, ciúme e dúvida.' AS descricao,
        1899 AS ano_publicacao,
        '9788535910663' AS isbn,
        4 AS quantidade_disponivel
    UNION ALL SELECT
        'b1d2e3f4-0002-4000-8000-000000000002',
        'Romance',
        'Orgulho e Preconceito',
        'Jane Austen',
        'Elizabeth Bennet e Mr. Darcy enfrentam primeiras impressões e convenções sociais na Inglaterra do século XIX.',
        1813,
        '9788544001820',
        3
    UNION ALL SELECT
        'b1d2e3f4-0003-4000-8000-000000000003',
        'Fantasia',
        'O Pequeno Príncipe',
        'Antoine de Saint-Exupéry',
        'Uma fábula sobre amizade, afeto e as coisas essenciais da vida.',
        1943,
        '9788522031443',
        5
    UNION ALL SELECT
        'b1d2e3f4-0004-4000-8000-000000000004',
        'Ficção',
        '1984',
        'George Orwell',
        'Uma distopia sobre vigilância, liberdade e poder em uma sociedade controlada pelo Grande Irmão.',
        1949,
        '9788535914845',
        2
    UNION ALL SELECT
        'b1d2e3f4-0005-4000-8000-000000000005',
        'Clássicos',
        'A Hora da Estrela',
        'Clarice Lispector',
        'Macabéa, uma jovem nordestina no Rio de Janeiro, vive uma trajetória narrada por Rodrigo S. M.',
        1977,
        '9788532508129',
        3
    UNION ALL SELECT
        'b1d2e3f4-0006-4000-8000-000000000006',
        'Fantasia',
        'O Hobbit',
        'J. R. R. Tolkien',
        'Bilbo Bolseiro deixa sua toca confortável e parte em uma aventura com anões e um mago.',
        1937,
        '9788595084742',
        2
    UNION ALL SELECT
        'b1d2e3f4-0007-4000-8000-000000000007',
        'Ficção',
        'A Revolução dos Bichos',
        'George Orwell',
        'Uma fábula satírica sobre poder e desigualdade em uma fazenda cujos animais se rebelam.',
        1945,
        '9788535909551',
        4
    UNION ALL SELECT
        'b1d2e3f4-0008-4000-8000-000000000008',
        'Romance',
        'Persuasão',
        'Jane Austen',
        'Anne Elliot reencontra um amor do passado em uma história sobre escolhas e segundas oportunidades.',
        1817,
        '9788544002131',
        1
) AS dados
INNER JOIN generos AS genero
    ON genero.nome = dados.genero;

COMMIT;

-- Conferência: deve listar os livros cadastrados e seus respectivos gêneros.
SELECT
    livro.id,
    livro.titulo,
    livro.autor,
    genero.nome AS genero,
    livro.ano_publicacao,
    livro.isbn,
    livro.quantidade_disponivel,
    livro.status_livro
FROM livros AS livro
INNER JOIN generos AS genero
    ON genero.id = livro.generos_id
WHERE livro.isbn IN (
    '9788535910663',
    '9788544001820',
    '9788522031443',
    '9788535914845',
    '9788532508129',
    '9788595084742',
    '9788535909551',
    '9788544002131'
)
ORDER BY livro.titulo;
