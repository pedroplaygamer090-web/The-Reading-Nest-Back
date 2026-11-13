package com.br.The_Reading_Nest_Back.repositories;

import com.br.The_Reading_Nest_Back.dtos.GeneroResponseDTO;
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
 * e apagamos categorias existentes.
 *
 * Explicação técnica:
 * Componente anotado com @Repository que encapsula o acesso à tabela 'generos' utilizando exclusivamente
 * o Spring JdbcTemplate. Todas as consultas são parametrizadas com placeholders '?' para mitigar
 * integralmente ataques de SQL Injection, com projeção explícita de colunas (ausência de SELECT *),
 * paginação via LIMIT e OFFSET, e conversão dos resultados através de instâncias reutilizáveis de RowMapper.
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

    /**
     * Mapeador que projeta a linha do banco diretamente em um GeneroResponseDTO.
     */
    private final RowMapper<GeneroResponseDTO> generoResponseRowMapper = (rs, rowNum) -> new GeneroResponseDTO(
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
     * @return Página (Page) contendo os registros da janela atual e a contagem total de itens.
     */
    public Page<GeneroResponseDTO> findAll(Pageable pageable) {
        String sqlCount = "SELECT COUNT(id) FROM generos";
        Long total = jdbcTemplate.queryForObject(sqlCount, Long.class);
        long totalRegistros = total != null ? total : 0L;

        String sql = "SELECT id, nome FROM generos ORDER BY nome ASC LIMIT ? OFFSET ?";
        List<GeneroResponseDTO> generos = jdbcTemplate.query(
                sql,
                generoResponseRowMapper,
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
     * @return Verdadeiro se o nome já estiver em uso, falso caso contrário.
     */
    public boolean existsByNome(String nome) {
        String sql = "SELECT COUNT(id) FROM generos WHERE LOWER(nome) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nome);
        return count != null && count > 0;
    }

    /**
     * Verifica se existe outro gênero com o mesmo nome, desconsiderando o ID informado (usado na edição).
     *
     * @param nome Nome pretendido.
     * @param id ID do registro atual em alteração.
     * @return Verdadeiro se houver conflito de duplicidade com outro ID.
     */
    public boolean existsByNomeAndIdNot(String nome, String id) {
        String sql = "SELECT COUNT(id) FROM generos WHERE LOWER(nome) = LOWER(?) AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nome, id);
        return count != null && count > 0;
    }

    /**
     * Atualiza as informações de um gênero existente.
     *
     * @param genero Entidade com os novos dados e o respectivo UUID.
     * @return Quantidade de linhas modificadas no banco.
     */
    public int update(Genero genero) {
        String sql = "UPDATE generos SET nome = ? WHERE id = ?";
        return jdbcTemplate.update(sql, genero.nome(), genero.id());
    }

    /**
     * Exclui fisicamente um gênero do acervo pelo seu identificador UUID.
     *
     * @param id Identificador do gênero.
     * @return Quantidade de linhas excluídas.
     */
    public int deleteById(String id) {
        String sql = "DELETE FROM generos WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
