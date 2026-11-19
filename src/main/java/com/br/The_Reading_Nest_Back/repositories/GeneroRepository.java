package com.br.The_Reading_Nest_Back.repositories;

import com.br.The_Reading_Nest_Back.entities.Genero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Explicação simples:
 * Responsável por conversar diretamente com a tabela de categorias e gêneros literários no banco de dados.
 * Aqui nós inserimos novos gêneros, consultamos a lista de forma organizada e paginada, alteramos nomes
 * e apagamos categorias existentes, devolvendo sempre a entidade Genero pura.
 *
 * Explicação técnica:
 * Componente anotado com @Repository que encapsula o acesso à tabela 'generos' utilizando exclusivamente
 * o Spring JdbcTemplate. Todas as consultas são parametrizadas com placeholders '?' para mitigar
 * integralmente ataques de SQL Injection, com projeção explícita de colunas (ausência de SELECT *),
 * paginação via LIMIT e OFFSET, e conversão dos resultados através do RowMapper da entidade pura Genero.
 */
@Repository
public class GeneroRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Mapeador que transforma cada linha retornada do banco em uma entidade Genero.
     */
    private final RowMapper<Genero> generoRowMapper = (rs, rowNum) -> new Genero(
            rs.getString("id"),
            rs.getString("nome")
    );

    public GeneroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Insere um novo gênero literário no banco de dados.
     *
     * @param genero Objeto da entidade contendo UUID e nome.
     * @return Quantidade de linhas afetadas na tabela (esperado: 1).
     */
    public int insert(Genero genero) {
        String sql = "INSERT INTO generos (id, nome) VALUES (?, ?)";
        return jdbcTemplate.update(sql, genero.id(), genero.nome());
    }

    /**
     * Busca todos os gêneros de forma paginada ordenados em ordem alfabética.
     *
     * @param pageable Configuração de página (número da página e tamanho).
     * @return Página (Page) contendo as entidades Genero da janela atual e a contagem total de itens.
     */
    public Page<Genero> findAll(Pageable pageable) {
        String sqlCount = "SELECT COUNT(id) FROM generos";
        Long total = jdbcTemplate.queryForObject(sqlCount, Long.class);
        long totalRegistros = total != null ? total : 0L;

        String sql = "SELECT id, nome FROM generos ORDER BY nome ASC LIMIT ? OFFSET ?";
        List<Genero> generos = jdbcTemplate.query(
                sql,
                generoRowMapper,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(generos, pageable, totalRegistros);
    }

    /**
     * Busca um gênero literário pelo seu identificador UUID universal.
     *
     * @param id Identificador UUID do gênero literário.
     * @return Optional contendo a entidade Genero caso encontrada, ou Optional.empty() caso contrário.
     */
    public Optional<Genero> findById(String id) {
        String sql = "SELECT id, nome FROM generos WHERE id = ?";
        List<Genero> resultado = jdbcTemplate.query(sql, generoRowMapper, id);
        return resultado.stream().findFirst();
    }

    /**
     * Verifica se já existe um gênero com o mesmo nome no banco de dados (ignorando maiúsculas/minúsculas).
     *
     * @param nome Nome do gênero pesquisado.
     * @return Verdadeiro se existir duplicidade.
     */
    public boolean existsByNome(String nome) {
        String sql = "SELECT COUNT(id) FROM generos WHERE LOWER(nome) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nome);
        return count != null && count > 0;
    }

    /**
     * Verifica se já existe outro gênero com o mesmo nome, excetuando o registro com o ID fornecido (para edição).
     *
     * @param nome Novo nome desejado.
     * @param id ID do gênero atual em edição.
     * @return Verdadeiro se colidir com outro registro.
     */
    public boolean existsByNomeAndIdNot(String nome, String id) {
        String sql = "SELECT COUNT(id) FROM generos WHERE LOWER(nome) = LOWER(?) AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nome, id);
        return count != null && count > 0;
    }

    /**
     * Atualiza o nome de um gênero já existente.
     *
     * @param genero Entidade com os dados modificados.
     * @return Quantidade de linhas modificadas no banco.
     */
    public int update(Genero genero) {
        String sql = "UPDATE generos SET nome = ? WHERE id = ?";
        return jdbcTemplate.update(sql, genero.nome(), genero.id());
    }

    /**
     * Exclui um gênero da base de dados.
     *
     * @param id Identificador UUID da categoria.
     * @return Quantidade de linhas afetadas.
     */
    public int deleteById(String id) {
        String sql = "DELETE FROM generos WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    /**
     * Verifica se existem livros vinculados a um determinado gênero literário antes de permitir sua exclusão.
     *
     * @param id Identificador UUID do gênero.
     * @return Verdadeiro se houver livros referenciando esta categoria.
     */
    public boolean hasLivrosVinculados(String id) {
        String sql = "SELECT COUNT(id) FROM livros WHERE generos_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
