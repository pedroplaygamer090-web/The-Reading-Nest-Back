package com.br.The_Reading_Nest_Back.repositories;

import com.br.The_Reading_Nest_Back.entities.Livro;
import com.br.The_Reading_Nest_Back.entities.StatusLivro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Explicação simples:
 * Gerencia a comunicação direta com a tabela de livros no banco de dados. É aqui que os livros
 * são salvos, buscados (com filtros por categoria e status), atualizados, desativados e onde o estoque
 * de exemplares disponíveis é controlado com total segurança para não ficar negativo, retornando sempre entidades Livro.
 *
 * Explicação técnica:
 * Repositório baseado em JdbcTemplate que interage com a tabela 'livros' e realiza junções (INNER JOIN)
 * com a tabela 'generos'. Emprega consultas dinâmicas parametrizadas com filtros opcionais, listagem
 * explícita de cada coluna para evitar o antipattern SELECT *, projeção via RowMapper para a entidade pura Livro
 * e comandos de atualização atômicos condicionais no banco de dados para evitar condições de corrida (race conditions).
 */
@Repository
public class LivroRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Mapeia as colunas da tabela 'livros' diretamente para a entidade pura 'Livro' (sem junção de gênero).
     */
    private final RowMapper<Livro> livroEntityRowMapper = (rs, rowNum) -> new Livro(
            rs.getString("id"),
            rs.getString("generos_id"),
            rs.getString("titulo"),
            rs.getString("autor"),
            rs.getString("descricao"),
            rs.getObject("ano_publicacao") != null ? rs.getInt("ano_publicacao") : null,
            rs.getString("isbn"),
            rs.getInt("quantidade_disponivel"),
            StatusLivro.valueOf(rs.getString("status_livro"))
    );

    /**
     * Mapeia a junção entre 'livros' e 'generos' projetando na entidade 'Livro' com o nome da categoria preenchido.
     */
    private final RowMapper<Livro> livroComGeneroRowMapper = (rs, rowNum) -> new Livro(
            rs.getString("id"),
            rs.getString("generos_id"),
            rs.getString("genero_nome"),
            rs.getString("titulo"),
            rs.getString("autor"),
            rs.getString("descricao"),
            rs.getObject("ano_publicacao") != null ? rs.getInt("ano_publicacao") : null,
            rs.getString("isbn"),
            rs.getInt("quantidade_disponivel"),
            StatusLivro.valueOf(rs.getString("status_livro"))
    );

    public LivroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Insere um novo livro no banco de dados.
     *
     * @param livro Entidade contendo os atributos do livro e seus identificadores.
     * @return Quantidade de linhas inseridas no banco.
     */
    public int insert(Livro livro) {
        String sql = "INSERT INTO livros (id, generos_id, titulo, autor, descricao, ano_publicacao, isbn, quantidade_disponivel, status_livro) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(
                sql,
                livro.id(),
                livro.generosId(),
                livro.titulo(),
                livro.autor(),
                livro.descricao(),
                livro.anoPublicacao(),
                livro.isbn(),
                livro.quantidadeDisponivel(),
                livro.statusLivro().name()
        );
    }

    /**
     * Consulta livros de forma paginada aplicando filtros opcionais de status do livro e gênero.
     *
     * @param status Filtro opcional por status ('ativo' ou 'desativado').
     * @param generoId Filtro opcional por identificador de categoria/gênero literário.
     * @param pageable Parâmetros de paginação e ordenação do Spring Data.
     * @return Página de Livro com totalizador e lista dos registros da janela atual.
     */
    public Page<Livro> findAll(StatusLivro status, String generoId, Pageable pageable) {
        StringBuilder whereSql = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            whereSql.append(" AND l.status_livro = ?");
            params.add(status.name());
        }

        if (generoId != null && !generoId.isBlank()) {
            whereSql.append(" AND l.generos_id = ?");
            params.add(generoId);
        }

        // 1. Contagem total com os mesmos filtros
        String countSql = "SELECT COUNT(l.id) FROM livros l" + whereSql;
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
        long totalRegistros = total != null ? total : 0L;

        // 2. Consulta dos registros da página com junção explícita
        String querySql = "SELECT l.id, l.generos_id, g.nome AS genero_nome, l.titulo, l.autor, "
                        + "l.descricao, l.ano_publicacao, l.isbn, l.quantidade_disponivel, l.status_livro "
                        + "FROM livros l "
                        + "INNER JOIN generos g ON l.generos_id = g.id"
                        + whereSql
                        + " ORDER BY l.titulo ASC LIMIT ? OFFSET ?";

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(pageable.getPageSize());
        queryParams.add(pageable.getOffset());

        List<Livro> livros = jdbcTemplate.query(
                querySql,
                livroComGeneroRowMapper,
                queryParams.toArray()
        );

        return new PageImpl<>(livros, pageable, totalRegistros);
    }

    /**
     * Busca um livro pelo seu ID trazendo também o nome legível de seu gênero associado.
     *
     * @param id Identificador UUID do livro.
     * @return Optional com a entidade Livro caso encontrada.
     */
    public Optional<Livro> findById(String id) {
        String sql = "SELECT l.id, l.generos_id, g.nome AS genero_nome, l.titulo, l.autor, "
                   + "l.descricao, l.ano_publicacao, l.isbn, l.quantidade_disponivel, l.status_livro "
                   + "FROM livros l "
                   + "INNER JOIN generos g ON l.generos_id = g.id "
                   + "WHERE l.id = ?";
        List<Livro> resultado = jdbcTemplate.query(sql, livroComGeneroRowMapper, id);
        return resultado.stream().findFirst();
    }

    /**
     * Busca a entidade básica do livro pelo ID (usada em verificações internas das regras de serviço).
     *
     * @param id Identificador UUID do livro.
     * @return Optional contendo a entidade pura Livro.
     */
    public Optional<Livro> findEntityById(String id) {
        String sql = "SELECT id, generos_id, titulo, autor, descricao, ano_publicacao, isbn, quantidade_disponivel, status_livro "
                   + "FROM livros WHERE id = ?";
        List<Livro> resultado = jdbcTemplate.query(sql, livroEntityRowMapper, id);
        return resultado.stream().findFirst();
    }

    /**
     * Checa se determinado código ISBN já está registrado na base de livros.
     *
     * @param isbn Código ISBN a verificar.
     * @return Verdadeiro se existir duplicidade.
     */
    public boolean existsByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(id) FROM livros WHERE isbn = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, isbn);
        return count != null && count > 0;
    }

    /**
     * Checa se determinado código ISBN já pertence a outro livro (para updates).
     *
     * @param isbn Código ISBN pretendido.
     * @param id Identificador UUID do livro em edição.
     * @return Verdadeiro se colidir com outro livro.
     */
    public boolean existsByIsbnAndIdNot(String isbn, String id) {
        if (isbn == null || isbn.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(id) FROM livros WHERE isbn = ? AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, isbn, id);
        return count != null && count > 0;
    }

    /**
     * Atualiza os dados de um livro existente.
     *
     * @param livro Entidade com dados modificados.
     * @return Quantidade de linhas alteradas.
     */
    public int update(Livro livro) {
        String sql = "UPDATE livros SET generos_id = ?, titulo = ?, autor = ?, descricao = ?, "
                   + "ano_publicacao = ?, isbn = ?, quantidade_disponivel = ?, status_livro = ? "
                   + "WHERE id = ?";
        return jdbcTemplate.update(
                sql,
                livro.generosId(),
                livro.titulo(),
                livro.autor(),
                livro.descricao(),
                livro.anoPublicacao(),
                livro.isbn(),
                livro.quantidadeDisponivel(),
                livro.statusLivro().name(),
                livro.id()
        );
    }

    /**
     * Realiza soft-delete atualizando o status do livro para 'desativado'.
     *
     * @param id Identificador UUID do livro.
     * @return Linhas alteradas no MySQL.
     */
    public int softDelete(String id) {
        String sql = "UPDATE livros SET status_livro = ? WHERE id = ?";
        return jdbcTemplate.update(sql, StatusLivro.desativado.name(), id);
    }

    /**
     * Decrementa atômica e condicionalmente 1 unidade do estoque disponível se quantidade_disponivel > 0.
     *
     * @param id Identificador UUID do livro.
     * @return 1 se o decremento foi bem-sucedido; 0 se o estoque estava zerado (evita estoque negativo).
     */
    public int decrementarEstoque(String id) {
        String sql = "UPDATE livros SET quantidade_disponivel = quantidade_disponivel - 1 "
                   + "WHERE id = ? AND quantidade_disponivel > 0";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * Incrementa atomicamente 1 unidade no estoque disponível ao registrar a devolução física de um livro.
     *
     * @param id Identificador UUID do livro devolvido.
     * @return Quantidade de linhas afetadas.
     */
    public int incrementarEstoque(String id) {
        String sql = "UPDATE livros SET quantidade_disponivel = quantidade_disponivel + 1 WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * Verifica se determinado livro está atrelado a algum aluguel com status não concluído ('ativo' ou 'atrasado').
     *
     * @param livroId Identificador UUID do livro.
     * @return Verdadeiro se houver aluguel pendente de devolução para este livro.
     */
    public boolean hasAlugueisAtivos(String livroId) {
        String sql = "SELECT COUNT(da.id) FROM detalhes_alugueis da "
                   + "INNER JOIN alugueis a ON da.alugueis_id = a.id "
                   + "WHERE da.livros_id = ? AND a.status IN ('ativo', 'atrasado')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, livroId);
        return count != null && count > 0;
    }
}
