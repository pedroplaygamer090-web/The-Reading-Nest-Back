package com.br.The_Reading_Nest_Back.repositories;

import com.br.The_Reading_Nest_Back.entities.PerfilUsuario;
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
 * Salva novos leitores, atualiza cadastros e perfis, realiza o desligamento de contas (soft-delete),
 * garante que a senha nunca seja lida em listagens ou consultas comuns e fornece método seguro para login,
 * retornando sempre a entidade Usuario.
 *
 * Explicação técnica:
 * Repositório baseado em JdbcTemplate que interage com a tabela 'usuarios'.
 * Cumpre a diretriz estrita de segurança B1 do projeto: o atributo confidencial 'senha'
 * é explicitamente omitido de todas as queries de listagem e perfil público, garantindo que
 * o hash nem sequer trafegue do banco de dados para a memória da aplicação nessas operações.
 * Projeta explicitamente todas as colunas (sem SELECT *), incluindo 'perfil' e 'status_conta',
 * devolvendo sempre a entidade pura Usuario.
 */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Mapeador seguro para listagens e perfis que não projeta a coluna confidencial 'senha' (deixando-a nula).
     */
    private final RowMapper<Usuario> usuarioListagemRowMapper = (rs, rowNum) -> new Usuario(
            rs.getString("id"),
            rs.getString("nome"),
            rs.getString("email"),
            null,
            PerfilUsuario.valueOf(rs.getString("perfil")),
            StatusConta.valueOf(rs.getString("status_conta"))
    );

    /**
     * Mapeador completo da entidade Usuario incluindo senha e perfil (para autenticação interna restrita).
     */
    private final RowMapper<Usuario> usuarioEntityRowMapper = (rs, rowNum) -> new Usuario(
            rs.getString("id"),
            rs.getString("nome"),
            rs.getString("email"),
            rs.getString("senha"),
            PerfilUsuario.valueOf(rs.getString("perfil")),
            StatusConta.valueOf(rs.getString("status_conta"))
    );

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Insere um novo usuário com senha criptografada em hash seguro e perfil de acesso definido.
     *
     * @param usuario Entidade com identificador UUID gerado e dados completos.
     * @return Linhas inseridas no banco.
     */
    public int insert(Usuario usuario) {
        String sql = "INSERT INTO usuarios (id, nome, email, senha, perfil, status_conta) VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(
                sql,
                usuario.id(),
                usuario.nome(),
                usuario.email(),
                usuario.senha(),
                usuario.perfil().name(),
                usuario.statusConta().name()
        );
    }

    /**
     * Consulta usuários de forma paginada em ordem alfabética sem selecionar o campo 'senha'.
     *
     * @param pageable Configurações de número de página e tamanho.
     * @return Página de entidades Usuario com total de registros.
     */
    public Page<Usuario> findAll(Pageable pageable) {
        String sqlCount = "SELECT COUNT(id) FROM usuarios";
        Long total = jdbcTemplate.queryForObject(sqlCount, Long.class);
        long totalRegistros = total != null ? total : 0L;

        String sql = "SELECT id, nome, email, perfil, status_conta FROM usuarios ORDER BY nome ASC LIMIT ? OFFSET ?";
        List<Usuario> usuarios = jdbcTemplate.query(
                sql,
                usuarioListagemRowMapper,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(usuarios, pageable, totalRegistros);
    }

    /**
     * Busca dados cadastrais do usuário pelo ID sem selecionar a senha.
     *
     * @param id Identificador UUID do usuário.
     * @return Optional contendo entidade Usuario segura.
     */
    public Optional<Usuario> findById(String id) {
        String sql = "SELECT id, nome, email, perfil, status_conta FROM usuarios WHERE id = ?";
        List<Usuario> resultado = jdbcTemplate.query(sql, usuarioListagemRowMapper, id);
        return resultado.stream().findFirst();
    }

    /**
     * Busca os dados internos completos do usuário pelo ID, incluindo a senha criptografada e o perfil.
     *
     * @param id Identificador UUID do usuário.
     * @return Optional contendo a entidade interna Usuario.
     */
    public Optional<Usuario> findEntityById(String id) {
        String sql = "SELECT id, nome, email, senha, perfil, status_conta FROM usuarios WHERE id = ?";
        List<Usuario> resultado = jdbcTemplate.query(sql, usuarioEntityRowMapper, id);
        return resultado.stream().findFirst();
    }

    /**
     * Busca os dados de um usuário pelo e-mail com senha incluída, utilizado exclusivamente pelo processo de autenticação.
     *
     * @param email E-mail único do usuário.
     * @return Optional contendo a entidade interna Usuario com credenciais completas.
     */
    public Optional<Usuario> findByEmailComSenha(String email) {
        String sql = "SELECT id, nome, email, senha, perfil, status_conta FROM usuarios WHERE LOWER(email) = LOWER(?)";
        List<Usuario> resultado = jdbcTemplate.query(sql, usuarioEntityRowMapper, email);
        return resultado.stream().findFirst();
    }

    /**
     * Verifica se determinado e-mail já foi registrado no sistema.
     *
     * @param email E-mail pretendido.
     * @return Verdadeiro se o e-mail já existir.
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(id) FROM usuarios WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Checa se determinado e-mail já pertence a outro usuário cadastrado (para cenários de atualização).
     *
     * @param email E-mail pretendido.
     * @param id Identificador do usuário em modificação.
     * @return Verdadeiro se colidir com outro cadastro.
     */
    public boolean existsByEmailAndIdNot(String email, String id) {
        String sql = "SELECT COUNT(id) FROM usuarios WHERE LOWER(email) = LOWER(?) AND id <> ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email, id);
        return count != null && count > 0;
    }

    /**
     * Atualiza os dados cadastrais e perfil de um usuário existente.
     *
     * @param usuario Entidade com dados atualizados.
     * @param atualizarSenha Se verdadeiro, inclui a nova senha com hash no comando UPDATE.
     * @return Linhas alteradas.
     */
    public int update(Usuario usuario, boolean atualizarSenha) {
        return update(usuario, atualizarSenha, true);
    }

    /**
     * Atualiza os dados cadastrais e controla separadamente a alteração da situação da conta.
     *
     * @param usuario Entidade com dados atualizados.
     * @param atualizarSenha Se verdadeiro, inclui a nova senha com hash no comando UPDATE.
     * @param atualizarStatusConta Se verdadeiro, inclui a situação da conta no comando UPDATE.
     * @return Linhas alteradas.
     */
    public int update(Usuario usuario, boolean atualizarSenha, boolean atualizarStatusConta) {
        if (atualizarSenha) {
            if (atualizarStatusConta) {
                String sql = "UPDATE usuarios SET nome = ?, email = ?, senha = ?, perfil = ?, status_conta = ? WHERE id = ?";
                return jdbcTemplate.update(
                        sql,
                        usuario.nome(),
                        usuario.email(),
                        usuario.senha(),
                        usuario.perfil().name(),
                        usuario.statusConta().name(),
                        usuario.id()
                );
            } else {
                String sql = "UPDATE usuarios SET nome = ?, email = ?, senha = ?, perfil = ? WHERE id = ?";
                return jdbcTemplate.update(
                        sql,
                        usuario.nome(),
                        usuario.email(),
                        usuario.senha(),
                        usuario.perfil().name(),
                        usuario.id()
                );
            }
        } else {
            if (atualizarStatusConta) {
                String sql = "UPDATE usuarios SET nome = ?, email = ?, perfil = ?, status_conta = ? WHERE id = ?";
                return jdbcTemplate.update(
                        sql,
                        usuario.nome(),
                        usuario.email(),
                        usuario.perfil().name(),
                        usuario.statusConta().name(),
                        usuario.id()
                );
            } else {
                String sql = "UPDATE usuarios SET nome = ?, email = ?, perfil = ? WHERE id = ?";
                return jdbcTemplate.update(
                        sql,
                        usuario.nome(),
                        usuario.email(),
                        usuario.perfil().name(),
                        usuario.id()
                );
            }
        }
    }

    /**
     * Realiza soft-delete atualizando a situação cadastral do usuário para 'desativado'.
     *
     * @param id Identificador UUID do usuário.
     * @return Linhas alteradas no MySQL.
     */
    public int softDelete(String id) {
        String sql = "UPDATE usuarios SET status_conta = ? WHERE id = ?";
        return jdbcTemplate.update(sql, StatusConta.desativado.name(), id);
    }

    /**
     * Checa se o usuário possui empréstimos ativos ou atrasados pendentes de devolução antes da exclusão.
     *
     * @param usuarioId Identificador UUID do usuário.
     * @return Verdadeiro se existirem pendências de empréstimo.
     */
    public boolean hasAlugueisAtivos(String usuarioId) {
        String sql = "SELECT COUNT(id) FROM alugueis WHERE usuarios_id = ? AND status IN ('ativo', 'atrasado')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, usuarioId);
        return count != null && count > 0;
    }
}
