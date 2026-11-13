package com.br.The_Reading_Nest_Back.controllers;

import com.br.The_Reading_Nest_Back.dtos.AluguelResumoResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioUpdateDTO;
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
 * e consultar o histórico de aluguéis do usuário para alimentar a tela 'Minha Conta'.
 *
 * Explicação técnica:
 * Controlador RESTful mapeado em '/usuarios'. Provê operações CRUD seguras de usuários e expõe
 * o sub-recurso '/usuarios/{id}/alugueis' para consulta paginada do histórico de empréstimos,
 * delegando as chamadas para UsuarioService e AluguelService sem expor a senha em nenhuma rota.
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AluguelService aluguelService;

    public UsuarioController(UsuarioService usuarioService, AluguelService aluguelService) {
        this.usuarioService = usuarioService;
        this.aluguelService = aluguelService;
    }

    /**
     * Cadastra um novo usuário no sistema da biblioteca com senha criptografada.
     *
     * @param dto Formulário validado com nome, e-mail e senha.
     * @return HTTP 201 Created com os dados públicos do usuário recém-criado.
     */
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO criado = usuarioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Retorna a listagem paginada de todos os usuários cadastrados.
     *
     * @param pageable Configurações globais de paginação.
     * @return HTTP 200 OK com a página de usuários.
     */
    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listar(Pageable pageable) {
        Page<UsuarioResponseDTO> pagina = usuarioService.listar(pageable);
        return ResponseEntity.ok(pagina);
    }

    /**
     * Busca dados cadastrais públicos de um usuário pelo seu ID.
     *
     * @param id Identificador UUID do usuário.
     * @return HTTP 200 OK com os dados do usuário.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable String id) {
        UsuarioResponseDTO usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Busca o histórico de empréstimos e devoluções de um leitor específico (tela Minha Conta).
     *
     * @param id Identificador UUID do usuário.
     * @param pageable Configurações de paginação.
     * @return HTTP 200 OK com a página de aluguéis resumidos do leitor.
     */
    @GetMapping("/{id}/alugueis")
    public ResponseEntity<Page<AluguelResumoResponseDTO>> listarAlugueis(
            @PathVariable String id,
            Pageable pageable
    ) {
        Page<AluguelResumoResponseDTO> alugueis = aluguelService.listarPorUsuario(id, pageable);
        return ResponseEntity.ok(alugueis);
    }

    /**
     * Atualiza dados de um usuário (nome, e-mail, status e, opcionalmente, senha).
     *
     * @param id Identificador UUID do usuário.
     * @param dto Dados para atualização cadastral.
     * @return HTTP 200 OK com dados atualizados do usuário.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody UsuarioUpdateDTO dto
    ) {
        UsuarioResponseDTO atualizado = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
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
