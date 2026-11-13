package com.br.The_Reading_Nest_Back.repositories;

import com.br.The_Reading_Nest_Back.dtos.UsuarioResponseDTO;
import com.br.The_Reading_Nest_Back.entities.StatusConta;
import com.br.The_Reading_Nest_Back.entities.Usuario;
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
 * Cuida das operações de leitura e gravação da tabela de usuários da biblioteca.
 * Salva novos leitores, atualiza perfis, realiza o desligamento de contas (soft-delete)
 * e garante que a senha nunca seja lida em listagens ou consultas comuns de perfil.
 *
 * Explicação técnica:
 * Repositório baseado em JdbcTemplate que interage com a tabela 'usuarios'.
 * Cumpre a diretriz estrita de segurança B1 do code review: o atributo confidencial 'senha'
 * é explicitamente omitido de todas as queries de listagem e perfil público, garantindo que
 * o hash nem sequer trafegue do banco de dados para a memória da aplicação nessas operações.
 */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Mapeador seguro para respostas públicas que não projeta a coluna confidencial 'senha'.
     */
    private final RowMapper<UsuarioResponseDTO> usuarioResponseRowMapper = (rs, rowNum) -> new UsuarioResponseDTO(
            rs.getString("id"),
            rs.getString("nome"),
            rs.getString("email"),
            StatusConta.valueOf(rs.getString("status_conta"))
    );

    /**
     * Mapeador completo da entidade Usuario incluindo senha (para uso interno restrito de autenticação).
     */
    private final RowMapper<Usuario> usuarioEntityRowMapper = (rs, rowNum) -> new Usuario(
            rs.getString("id"),
            rs.getString("nome"),
            rs.getString("email"),
            rs.getString("senha"),
            StatusConta.valueOf(rs.getString("status_conta"))
    );

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Insere um novo usuário com senha já criptografada em hash seguro.
     *
     * @param usuario Entidade com identificador UUID gerado e dados completos.
     * @return Linhas inseridas no banco.
     */
    public int insert(Usuario usuario) {
        String sql = "INSERT INTO usuarios (id, nome, email, senha, status_conta) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(
                sql,
                usuario.id(),
                usuario.nome(),
                usuario.email(),
                usuario.senha(),
                usuario.statusConta().name()
        );
    }

    /**
     * Consulta usuários de forma paginada em ordem alfabética sem selecionar o campo 'senha'.
     *
     * @param pageable Configurações de número de página e tamanho.
     * @return Página de UsuarioResponseDTO com total de registros.
     */
    public Page<UsuarioResponseDTO> findAll(Pageable pageable) {
        String sqlCount = "SELECT COUNT(id) FROM usuarios";
        Long total = jdbcTemplate.queryForObject(sqlCount, Long.class);
        long totalRegistros = total != null ? total : 0L;

        String sql = "SELECT id, nome, email, status_conta FROM usuarios ORDER BY nome ASC LIMIT ? OFFSET ?";
        List<UsuarioResponseDTO> usuarios = jdbcTemplate.query(
                sql,
                usuarioResponseRowMapper,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(usuarios, pageable, totalRegistros);
    }

    /**
     * Busca dados públicos do usuário pelo ID sem selecionar a senha.
     *
     * @param id Identificador UUID do usuário.
     * @return Optional contendo UsuarioResponseDTO seguro.
     */
    public Optional<UsuarioResponseDTO> findById(String id) {
        String sql = "SELECT id, nome, email, status_conta FROM usuarios WHERE id = ?";
        List<UsuarioResponseDTO> resultado = jdbcTemplate.query(sql, usuarioResponseRowMapper, id);
        return resultado.stream().findFirst();
    }

    /**
     * Busca os dados internos completos do usuário pelo ID, incluindo a senha criptografada.
     *
     * @param id Identificador UUID do usuário.
     * @return Optional contendo a entidade interna Usuario.
     */
    public Optional<Usuario> findEntityById(String id) {
        String sql = "SELECT id, nome, email, senha, status_conta FROM usuarios WHERE id = ?";
        List<Usuario> resultado = jdbcTemplate.query(sql, usuarioEntityRowMapper, id);
        return resultado.stream().findFirst();
    }

    /**
     * Verifica se determinado e-mail já foi registrado no sistema.
     *
     * @param email E-mail a ser pesquisado.
     * @return Verdadeiro caso o e-mail já exista.
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(id) FROM usuarios WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Verifica se determinado e-mail já pertence a outro usuário diferente do ID fornecido.
     *
     * @param email E-mail pretendido.
     * @param id ID do usuário atual.
     * @return Verdadeiro caso haja duplicidade.
     */
    public boolean existsByEmailAndIdNot(String email, String id) {
        String sql = "SELECT COUNT(id) FROM usuarios WHERE LOWER(email) = LOWER(?) AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email, id);
        return count != null && count > 0;
    }

    /**
     * Atualiza os dados de cadastro de um usuário, alterando a senha somente se requisitado.
     *
     * @param usuario Entidade com os dados modificados.
     * @param atualizarSenha Flag booleana indicando se o campo 'senha' deve ser atualizado.
     * @return Quantidade de linhas alteradas.
     */
    public int update(Usuario usuario, boolean atualizarSenha) {
        if (atualizarSenha) {
            String sql = "UPDATE usuarios SET nome = ?, email = ?, senha = ?, status_conta = ? WHERE id = ?";
            return jdbcTemplate.update(
                    sql,
                    usuario.nome(),
                    usuario.email(),
                    usuario.senha(),
                    usuario.statusConta().name(),
                    usuario.id()
            );
        } else {
            String sql = "UPDATE usuarios SET nome = ?, email = ?, status_conta = ? WHERE id = ?";
            return jdbcTemplate.update(
                    sql,
                    usuario.nome(),
                    usuario.email(),
                    usuario.statusConta().name(),
                    usuario.id()
            );
        }
    }

    /**
     * Atualiza apenas o status da conta do usuário (ativo, suspenso ou desativado).
     *
     * @param id Identificador UUID do usuário.
     * @param status Novo status da conta.
     * @return Linhas alteradas.
     */
    public int updateStatus(String id, StatusConta status) {
        String sql = "UPDATE usuarios SET status_conta = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status.name(), id);
    }
}
