package com.br.The_Reading_Nest_Back.services;

import com.br.The_Reading_Nest_Back.dtos.UsuarioRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioUpdateDTO;
import com.br.The_Reading_Nest_Back.entities.PerfilUsuario;
import com.br.The_Reading_Nest_Back.entities.StatusConta;
import com.br.The_Reading_Nest_Back.entities.Usuario;
import com.br.The_Reading_Nest_Back.exceptions.CustomException;
import com.br.The_Reading_Nest_Back.mappers.UsuarioMapper;
import com.br.The_Reading_Nest_Back.repositories.UsuarioRepository;
import com.br.The_Reading_Nest_Back.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Explicação simples:
 * O cérebro do gerenciamento de leitores e administradores da biblioteca.
 * Cuida para que ninguém use o mesmo e-mail duas vezes, criptografa a senha de todos,
 * assegura que apenas administradores possam criar ou promover contas de administrador,
 * e garante que um cliente comum só consiga ver e editar os seus próprios dados cadastrais, retornando entidades Usuario.
 *
 * Explicação técnica:
 * Camada de serviço de domínio para gerenciamento de usuários.
 * Integra transações declarativas (@Transactional), hashing BCrypt, verificação de integridade
 * de chave única (e-mail) e controle de acesso granular baseado no usuário autenticado (SecurityUtils),
 * devolvendo sempre a entidade pura Usuario.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder,
            SecurityUtils securityUtils
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    /**
     * Cadastra um novo usuário no sistema com perfil validado e senha criptografada.
     *
     * @param dto Formulário com dados do usuário (nome, e-mail, senha e perfil opcional).
     * @return Entidade Usuario cadastrada.
     */
    @Transactional
    public Usuario criar(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email().trim())) {
            throw new CustomException(HttpStatus.CONFLICT, "JÁ EXISTE UM USUÁRIO CADASTRADO COM ESTE E-MAIL");
        }

        // Se solicitou perfil 'admin', exige que o requisitante seja administrador autenticado
        PerfilUsuario perfilFinal;
        if (dto.perfil() != null && dto.perfil() == PerfilUsuario.admin) {
            if (!securityUtils.isAdmin()) {
                throw new CustomException(HttpStatus.FORBIDDEN, "APENAS ADMINISTRADORES PODEM DEFINIR PERFIL ADMIN");
            }
            perfilFinal = PerfilUsuario.admin;
        } else {
            // Visitante ou cadastro público sem perfil definido recebe 'cliente' por padrão
            perfilFinal = PerfilUsuario.cliente;
        }

        String id = UUID.randomUUID().toString();
        String senhaHash = passwordEncoder.encode(dto.senha());
        Usuario usuario = usuarioMapper.toEntity(id, dto, senhaHash, perfilFinal);
        usuarioRepository.insert(usuario);

        return usuario;
    }

    /**
     * Retorna a listagem paginada de todos os usuários cadastrados (sem as senhas).
     *
     * @param pageable Configurações de paginação do Spring Data.
     * @return Página de entidades Usuario.
     */
    public Page<Usuario> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    /**
     * Busca dados cadastrais públicos de um usuário pelo seu ID, validando se o cliente é o proprietário.
     *
     * @param id Identificador UUID do usuário.
     * @return Entidade Usuario localizada.
     */
    public Usuario buscarPorId(String id) {
        // Validação de posse: cliente só pode consultar seu próprio ID
        securityUtils.getUsuarioAutenticado().ifPresent(usuarioLogado -> {
            if (usuarioLogado.perfil() == PerfilUsuario.cliente && !usuarioLogado.id().equals(id)) {
                throw new CustomException(HttpStatus.FORBIDDEN, "ACESSO NEGADO: VOCÊ SÓ PODE ACESSAR SEUS PRÓPRIOS DADOS");
            }
        });

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "USUÁRIO NÃO ENCONTRADO"));
    }

    /**
     * Atualiza dados de cadastro de um usuário, impedindo auto-promoção indevida ou alteração de dados alheios.
     *
     * @param id Identificador UUID do usuário a ser atualizado.
     * @param dto Formulário de atualização.
     * @return Entidade Usuario atualizada.
     */
    @Transactional
    public Usuario atualizar(String id, UsuarioUpdateDTO dto) {
        Usuario usuarioAtual = usuarioRepository.findEntityById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "USUÁRIO NÃO ENCONTRADO"));

        // Validação de regras e posse pelo usuário autenticado
        boolean solicitanteIsAdmin = securityUtils.isAdmin();
        securityUtils.getUsuarioAutenticado().ifPresent(usuarioLogado -> {
            if (usuarioLogado.perfil() == PerfilUsuario.cliente) {
                if (!usuarioLogado.id().equals(id)) {
                    throw new CustomException(HttpStatus.FORBIDDEN, "ACESSO NEGADO: VOCÊ SÓ PODE ACESSAR SEUS PRÓPRIOS DADOS");
                }
                if (dto.perfil() != null && dto.perfil() == PerfilUsuario.admin) {
                    throw new CustomException(HttpStatus.FORBIDDEN, "APENAS ADMINISTRADORES PODEM DEFINIR PERFIL ADMIN");
                }
            }
        });

        if (usuarioRepository.existsByEmailAndIdNot(dto.email().trim(), id)) {
            throw new CustomException(HttpStatus.CONFLICT, "JÁ EXISTE UM USUÁRIO CADASTRADO COM ESTE E-MAIL");
        }

        boolean atualizarSenha = dto.senha() != null && !dto.senha().isBlank();
        String senhaFinal = atualizarSenha ? passwordEncoder.encode(dto.senha()) : usuarioAtual.senha();

        // Cliente não pode alterar o próprio perfil nem reativar conta suspensa/desativada
        PerfilUsuario perfilFinal;
        StatusConta statusFinal;
        if (solicitanteIsAdmin) {
            perfilFinal = dto.perfil() != null ? dto.perfil() : usuarioAtual.perfil();
            statusFinal = dto.statusConta() != null && !dto.statusConta().isBlank()
                    ? converterStatusConta(dto.statusConta())
                    : usuarioAtual.statusConta();
        } else {
            perfilFinal = usuarioAtual.perfil();
            statusFinal = usuarioAtual.statusConta();
        }

        Usuario usuarioAtualizado = new Usuario(
                id,
                dto.nome().trim(),
                dto.email().trim().toLowerCase(),
                senhaFinal,
                perfilFinal,
                statusFinal
        );

        usuarioRepository.update(usuarioAtualizado, atualizarSenha, solicitanteIsAdmin);

        return usuarioAtualizado;
    }

    private StatusConta converterStatusConta(String statusConta) {
        return switch (statusConta.trim()) {
            case "ativo" -> StatusConta.ativo;
            case "suspenso" -> StatusConta.suspenso;
            case "desativado" -> StatusConta.desativado;
            default -> throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "STATUS DA CONTA INVÁLIDO; USE ativo, suspenso ou desativado"
            );
        };
    }

    /**
     * Desativa uma conta de usuário no sistema (soft-delete).
     *
     * @param id Identificador UUID do usuário a ser desativado.
     */
    @Transactional
    public void excluir(String id) {
        // Validação de posse: cliente só pode excluir/desativar a própria conta
        securityUtils.getUsuarioAutenticado().ifPresent(usuarioLogado -> {
            if (usuarioLogado.perfil() == PerfilUsuario.cliente && !usuarioLogado.id().equals(id)) {
                throw new CustomException(HttpStatus.FORBIDDEN, "ACESSO NEGADO: VOCÊ SÓ PODE ACESSAR SEUS PRÓPRIOS DADOS");
            }
        });

        if (usuarioRepository.findById(id).isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "USUÁRIO NÃO ENCONTRADO");
        }

        if (usuarioRepository.hasAlugueisAtivos(id)) {
            throw new CustomException(HttpStatus.CONFLICT, "NÃO É POSSÍVEL DESATIVAR USUÁRIO COM ALUGUÉIS PENDENTES DE DEVOLUÇÃO");
        }

        usuarioRepository.softDelete(id);
    }
}
