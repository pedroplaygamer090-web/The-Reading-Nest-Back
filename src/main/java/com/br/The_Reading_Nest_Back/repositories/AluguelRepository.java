package com.br.The_Reading_Nest_Back.repositories;

import com.br.The_Reading_Nest_Back.dtos.AluguelDetalhadoResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.AluguelResumoResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.LivroResponseDTO;
import com.br.The_Reading_Nest_Back.entities.Aluguel;
import com.br.The_Reading_Nest_Back.entities.DetalheAluguel;
import com.br.The_Reading_Nest_Back.entities.StatusAluguel;
import com.br.The_Reading_Nest_Back.entities.StatusLivro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Explicação simples:
 * Gerencia todas as tabelas de aluguéis e empréstimos da biblioteca ('alugueis' e 'detalhes_alugueis').
 * Registra quando um leitor retira livros, busca os aluguéis pendentes para o painel do administrador,
 * recupera o histórico da conta do leitor e atualiza os prazos e devoluções.
 *
 * Explicação técnica:
 * Repositório que orquestra a persistência relacional pura via JdbcTemplate entre a entidade-mestre 'alugueis'
 * e os itens associados em 'detalhes_alugueis'. Realiza projeções otimizadas para leitura rápida sem carregar
 * dados redundantes em listagens e consolida consultas com INNER JOIN nas tabelas 'usuarios', 'livros' e 'generos'
 * para composição de DTOs detalhados.
 */
@Repository
public class AluguelRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Mapeador para projeção resumida de aluguel (cabeçalho + nome do usuário).
     */
    private final RowMapper<AluguelResumoResponseDTO> aluguelResumoRowMapper = (rs, rowNum) -> new AluguelResumoResponseDTO(
            rs.getString("id"),
            rs.getString("usuarios_id"),
            rs.getString("usuario_nome"),
            rs.getTimestamp("data_aluguel").toLocalDateTime(),
            rs.getDate("data_devolucao_prevista").toLocalDate(),
            StatusAluguel.valueOf(rs.getString("status"))
    );

    /**
     * Mapeador para a entidade base Aluguel.
     */
    private final RowMapper<Aluguel> aluguelEntityRowMapper = (rs, rowNum) -> new Aluguel(
            rs.getString("id"),
            rs.getString("usuarios_id"),
            rs.getTimestamp("data_aluguel").toLocalDateTime(),
            rs.getDate("data_devolucao_prevista").toLocalDate(),
            StatusAluguel.valueOf(rs.getString("status"))
    );

    /**
     * Mapeador para projeção de LivroResponseDTO nos livros vinculados a um aluguel.
     */
    private final RowMapper<LivroResponseDTO> livroResponseRowMapper = (rs, rowNum) -> new LivroResponseDTO(
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

    public AluguelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Insere o registro principal (cabeçalho) do aluguel na tabela 'alugueis'.
     *
     * @param aluguel Entidade contendo o UUID, identificador do usuário, datas e status.
     * @return Linhas afetadas.
     */
    public int insertAluguel(Aluguel aluguel) {
        String sql = "INSERT INTO alugueis (id, usuarios_id, data_aluguel, data_devolucao_prevista, status) "
                   + "VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(
                sql,
                aluguel.id(),
                aluguel.usuariosId(),
                Timestamp.valueOf(aluguel.dataAluguel()),
                java.sql.Date.valueOf(aluguel.dataDevolucaoPrevista()),
                aluguel.status().name()
        );
    }

    /**
     * Insere um registro de vínculo na tabela associativa 'detalhes_alugueis'.
     *
     * @param detalhe Entidade associativa contendo UUID, ID do aluguel e ID do livro.
     * @return Linhas afetadas.
     */
    public int insertDetalheAluguel(DetalheAluguel detalhe) {
        String sql = "INSERT INTO detalhes_alugueis (id, alugueis_id, livros_id) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, detalhe.id(), detalhe.alugueisId(), detalhe.livrosId());
    }

    /**
     * Busca todos os aluguéis não concluídos (status 'ativo' ou 'atrasado'), ordenando
     * prioritariamente pelos prazos mais antigos e urgentes de devolução.
     *
     * @param pageable Configuração de paginação.
     * @return Página de AluguelResumoResponseDTO.
     */
    public Page<AluguelResumoResponseDTO> findNaoConcluidos(Pageable pageable) {
        String countSql = "SELECT COUNT(id) FROM alugueis WHERE status IN ('ativo', 'atrasado')";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);
        long totalRegistros = total != null ? total : 0L;

        String querySql = "SELECT a.id, a.usuarios_id, u.nome AS usuario_nome, a.data_aluguel, "
                        + "a.data_devolucao_prevista, a.status "
                        + "FROM alugueis a "
                        + "INNER JOIN usuarios u ON a.usuarios_id = u.id "
                        + "WHERE a.status IN ('ativo', 'atrasado') "
                        + "ORDER BY a.data_devolucao_prevista ASC, a.data_aluguel ASC "
                        + "LIMIT ? OFFSET ?";

        List<AluguelResumoResponseDTO> alugueis = jdbcTemplate.query(
                querySql,
                aluguelResumoRowMapper,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(alugueis, pageable, totalRegistros);
    }

    /**
     * Busca o histórico de aluguéis de um determinado usuário de forma paginada em ordem cronológica decrescente.
     *
     * @param usuarioId Identificador UUID do leitor.
     * @param pageable Parâmetros de paginação.
     * @return Página com o histórico do usuário.
     */
    public Page<AluguelResumoResponseDTO> findByUsuarioId(String usuarioId, Pageable pageable) {
        String countSql = "SELECT COUNT(id) FROM alugueis WHERE usuarios_id = ?";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, usuarioId);
        long totalRegistros = total != null ? total : 0L;

        String querySql = "SELECT a.id, a.usuarios_id, u.nome AS usuario_nome, a.data_aluguel, "
                        + "a.data_devolucao_prevista, a.status "
                        + "FROM alugueis a "
                        + "INNER JOIN usuarios u ON a.usuarios_id = u.id "
                        + "WHERE a.usuarios_id = ? "
                        + "ORDER BY a.data_aluguel DESC "
                        + "LIMIT ? OFFSET ?";

        List<AluguelResumoResponseDTO> alugueis = jdbcTemplate.query(
                querySql,
                aluguelResumoRowMapper,
                usuarioId,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(alugueis, pageable, totalRegistros);
    }

    /**
     * Busca os dados detalhados de um aluguel específico agregando a lista completa dos livros alugados.
     *
     * @param id Identificador UUID do aluguel.
     * @return Optional contendo AluguelDetalhadoResponseDTO com cabeçalho e livros.
     */
    public Optional<AluguelDetalhadoResponseDTO> findByIdComLivros(String id) {
        String sqlCabecalho = "SELECT a.id, a.usuarios_id, u.nome AS usuario_nome, a.data_aluguel, "
                            + "a.data_devolucao_prevista, a.status "
                            + "FROM alugueis a "
                            + "INNER JOIN usuarios u ON a.usuarios_id = u.id "
                            + "WHERE a.id = ?";

        List<AluguelResumoResponseDTO> cabecalhos = jdbcTemplate.query(sqlCabecalho, aluguelResumoRowMapper, id);
        if (cabecalhos.isEmpty()) {
            return Optional.empty();
        }

        AluguelResumoResponseDTO cabecalho = cabecalhos.getFirst();

        String sqlLivros = "SELECT l.id, l.generos_id, g.nome AS genero_nome, l.titulo, l.autor, "
                         + "l.descricao, l.ano_publicacao, l.isbn, l.quantidade_disponivel, l.status_livro "
                         + "FROM livros l "
                         + "INNER JOIN detalhes_alugueis da ON l.id = da.livros_id "
                         + "INNER JOIN generos g ON l.generos_id = g.id "
                         + "WHERE da.alugueis_id = ? "
                         + "ORDER BY l.titulo ASC";

        List<LivroResponseDTO> livros = jdbcTemplate.query(sqlLivros, livroResponseRowMapper, id);

        AluguelDetalhadoResponseDTO detalhado = new AluguelDetalhadoResponseDTO(
                cabecalho.id(),
                cabecalho.usuarioId(),
                cabecalho.usuarioNome(),
                cabecalho.dataAluguel(),
                cabecalho.dataDevolucaoPrevista(),
                cabecalho.status(),
                livros
        );

        return Optional.of(detalhado);
    }

    /**
     * Busca a entidade Aluguel pura para validações em regras transacionais de serviço.
     *
     * @param id Identificador UUID do aluguel.
     * @return Optional com a entidade Aluguel.
     */
    public Optional<Aluguel> findEntityById(String id) {
        String sql = "SELECT id, usuarios_id, data_aluguel, data_devolucao_prevista, status FROM alugueis WHERE id = ?";
        List<Aluguel> resultado = jdbcTemplate.query(sql, aluguelEntityRowMapper, id);
        return resultado.stream().findFirst();
    }

    /**
     * Recupera todos os identificadores de livros vinculados a determinado aluguel.
     *
     * @param aluguelId Identificador UUID do aluguel.
     * @return Lista de IDs dos livros associados.
     */
    public List<String> findLivrosIdsByAluguelId(String aluguelId) {
        String sql = "SELECT livros_id FROM detalhes_alugueis WHERE alugueis_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, aluguelId);
    }

    /**
     * Atualiza a data de devolução prevista e o status operacional do aluguel.
     *
     * @param id Identificador UUID do aluguel.
     * @param dataDevolucaoPrevista Nova data prevista.
     * @param status Novo status operacional.
     * @return Linhas alteradas no banco.
     */
    public int updateAluguel(String id, LocalDate dataDevolucaoPrevista, StatusAluguel status) {
        String sql = "UPDATE alugueis SET data_devolucao_prevista = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(
                sql,
                java.sql.Date.valueOf(dataDevolucaoPrevista),
                status.name(),
                id
        );
    }
}
