package com.br.The_Reading_Nest_Back.services;

import com.br.The_Reading_Nest_Back.dtos.UsuarioRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioUpdateDTO;
import com.br.The_Reading_Nest_Back.entities.StatusConta;
import com.br.The_Reading_Nest_Back.entities.Usuario;
import com.br.The_Reading_Nest_Back.exceptions.CustomException;
import com.br.The_Reading_Nest_Back.mappers.UsuarioMapper;
import com.br.The_Reading_Nest_Back.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Explicação simples:
 * O cérebro do gerenciamento de leitores e funcionários da biblioteca. Cuida para que ninguém
 * crie duas contas com o mesmo e-mail, protege a senha de todo mundo transformando-a em um código
 * seguro (hash criptográfico), atualiza cadastros e desativa contas sem apagar o histórico de leituras.
 *
 * Explicação técnica:
 * Camada de serviço responsável pelas operações de usuário. Integra o PasswordEncoder (BCrypt)
 * para realizar hashing unidirecional irreversível de senhas antes da gravação no banco,
 * valida unicidade de chave alternativa ('email'), implementa soft-delete via StatusConta.desativado
 * e orquestra transações com @Transactional.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cadastra um novo usuário no sistema com senha criptografada em hash seguro BCrypt.
     *
     * @param dto Formulário com nome, e-mail e senha em texto puro.
     * @return DTO com os dados do usuário cadastrado (sem a senha).
     */
    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email().trim())) {
            throw new CustomException(HttpStatus.CONFLICT, "JÁ EXISTE UM USUÁRIO CADASTRADO COM ESTE E-MAIL");
        }

        String id = UUID.randomUUID().toString();
        String senhaHash = passwordEncoder.encode(dto.senha());
        Usuario usuario = usuarioMapper.toEntity(id, dto, senhaHash);
        usuarioRepository.insert(usuario);

        return usuarioMapper.toResponseDTO(usuario);
    }

    /**
     * Retorna a listagem paginada de todos os usuários cadastrados (sem as senhas).
     *
     * @param pageable Configurações de paginação do Spring Data.
     * @return Página de UsuarioResponseDTO.
     */
    public Page<UsuarioResponseDTO> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    /**
     * Busca dados cadastrais públicos de um usuário pelo seu ID.
     *
     * @param id Identificador UUID do usuário.
     * @return DTO com os dados do usuário.
     */
    public UsuarioResponseDTO buscarPorId(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "USUÁRIO NÃO ENCONTRADO"));
    }

    /**
     * Atualiza dados do usuário (nome, e-mail, status e, opcionalmente, nova senha criptografada).
     *
     * @param id Identificador UUID do usuário a ser atualizado.
     * @param dto Formulário de atualização.
     * @return DTO atualizado do usuário.
     */
    @Transactional
    public UsuarioResponseDTO atualizar(String id, UsuarioUpdateDTO dto) {
        Usuario usuarioAtual = usuarioRepository.findEntityById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "USUÁRIO NÃO ENCONTRADO"));

        if (usuarioRepository.existsByEmailAndIdNot(dto.email().trim(), id)) {
            throw new CustomException(HttpStatus.CONFLICT, "JÁ EXISTE UM USUÁRIO CADASTRADO COM ESTE E-MAIL");
        }

        boolean atualizarSenha = dto.senha() != null && !dto.senha().isBlank();
        String senhaFinal = atualizarSenha ? passwordEncoder.encode(dto.senha()) : usuarioAtual.senha();
        StatusConta statusFinal = dto.statusConta() != null ? dto.statusConta() : usuarioAtual.statusConta();

        Usuario usuarioAtualizado = new Usuario(
                id,
                dto.nome().trim(),
                dto.email().trim().toLowerCase(),
                senhaFinal,
                statusFinal
        );

        usuarioRepository.update(usuarioAtualizado, atualizarSenha);

        return usuarioMapper.toResponseDTO(usuarioAtualizado);
    }

    /**
     * Desativa a conta de um usuário (soft-delete), mantendo seus empréstimos e histórico no sistema.
     *
     * @param id Identificador UUID do usuário a ser desativado.
     */
    @Transactional
    public void excluir(String id) {
        if (usuarioRepository.findById(id).isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "USUÁRIO NÃO ENCONTRADO");
        }

        usuarioRepository.updateStatus(id, StatusConta.desativado);
    }
}
