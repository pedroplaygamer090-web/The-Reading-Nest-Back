package com.br.The_Reading_Nest_Back.controllers;

import com.br.The_Reading_Nest_Back.dtos.AluguelResumoResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioUpdateDTO;
import com.br.The_Reading_Nest_Back.entities.Aluguel;
import com.br.The_Reading_Nest_Back.entities.Usuario;
import com.br.The_Reading_Nest_Back.mappers.AluguelMapper;
import com.br.The_Reading_Nest_Back.mappers.UsuarioMapper;
import com.br.The_Reading_Nest_Back.services.AluguelService;
import com.br.The_Reading_Nest_Back.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Explicação simples:
 * Ponto de entrada para operações com leitores e administradores da biblioteca.
 * Permite cadastrar um novo leitor, listar as contas cadastradas, consultar o perfil
 * e consultar o histórico de aluguéis do usuário para alimentar a tela 'Minha Conta',
 * utilizando os mappers para projetar os dados em DTOs seguros sem expor senhas.
 *
 * Explicação técnica:
 * Controlador RESTful mapeado em '/usuarios'. Provê operações CRUD seguras de usuários e expõe
 * o sub-recurso '/usuarios/{id}/alugueis' para consulta paginada do histórico de empréstimos,
 * delegando as chamadas para UsuarioService e AluguelService e convertendo as entidades resultantes
 * através de UsuarioMapper e AluguelMapper.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final AluguelService aluguelService;
    private final AluguelMapper aluguelMapper;

    public UsuarioController(
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper,
            AluguelService aluguelService,
            AluguelMapper aluguelMapper
    ) {
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
        this.aluguelService = aluguelService;
        this.aluguelMapper = aluguelMapper;
    }

    /**
     * Cadastra um novo usuário no sistema da biblioteca com senha criptografada.
     *
     * @param dto Formulário validado com nome, e-mail e senha.
     * @return HTTP 201 Created com os dados públicos do usuário convertidos via Mapper.
     */
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        Usuario criado = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.toResponseDTO(criado));
    }

    /**
     * Retorna a listagem paginada de todos os usuários cadastrados.
     *
     * @param pageable Configurações globais de paginação.
     * @return HTTP 200 OK com a página de usuários convertida via Mapper.
     */
    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listar(Pageable pageable) {
        Page<Usuario> pagina = usuarioService.listar(pageable);
        return ResponseEntity.ok(pagina.map(usuarioMapper::toResponseDTO));
    }

    /**
     * Busca dados cadastrais públicos de um usuário pelo seu ID.
     *
     * @param id Identificador UUID do usuário.
     * @return HTTP 200 OK com os dados do usuário convertidos via Mapper.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable String id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(usuario));
    }

    /**
     * Busca o histórico de empréstimos e devoluções de um leitor específico (tela Minha Conta).
     *
     * @param id Identificador UUID do usuário.
     * @param pageable Configurações de paginação.
     * @return HTTP 200 OK com a página de aluguéis resumidos convertida via Mapper.
     */
    @GetMapping("/{id}/alugueis")
    public ResponseEntity<Page<AluguelResumoResponseDTO>> listarAlugueis(
            @PathVariable String id,
            Pageable pageable
    ) {
        Page<Aluguel> alugueis = aluguelService.listarPorUsuario(id, pageable);
        return ResponseEntity.ok(alugueis.map(aluguelMapper::toResumoDTO));
    }

    /**
     * Atualiza dados de um usuário (nome, e-mail, status e, opcionalmente, senha).
     *
     * @param id Identificador UUID do usuário.
     * @param dto Dados para atualização cadastral.
     * @return HTTP 200 OK com dados atualizados convertidos via Mapper.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody UsuarioUpdateDTO dto
    ) {
        Usuario atualizado = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(atualizado));
    }

    /**
     * Desativa uma conta de usuário do sistema (soft-delete).
     *
     * @param id Identificador UUID do usuário.
     * @return HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
