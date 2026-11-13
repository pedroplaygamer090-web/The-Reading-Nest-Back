package com.br.The_Reading_Nest_Back.controllers;

import com.br.The_Reading_Nest_Back.dtos.GeneroRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.GeneroResponseDTO;
import com.br.The_Reading_Nest_Back.services.GeneroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Explicação simples:
 * Porta de entrada da internet para gerenciar categorias e gêneros de livros. Recebe os pedidos
 * enviados pelo navegador (como criar um gênero, listar todas as categorias, alterar ou apagar)
 * e repassa para a camada de serviço executar com segurança.
 *
 * Explicação técnica:
 * Controlador REST (@RestController) mapeado no plural para a rota '/generos' conforme padrão RESTful.
 * Valida os dados de entrada com @Valid disparando automaticamente o Bean Validation e delega
 * a execução estritamente para o 'GeneroService', sem acoplamento a regras de persistência ou de negócio.
 */
@RestController
@RequestMapping("/generos")
public class GeneroController {

    private final GeneroService generoService;

    public GeneroController(GeneroService generoService) {
        this.generoService = generoService;
    }

    /**
     * Cria um novo gênero literário na biblioteca.
     *
     * @param dto Corpo JSON validado contendo o nome da categoria.
     * @return HTTP 201 Created com os dados do gênero criado.
     */
    @PostMapping
    public ResponseEntity<GeneroResponseDTO> criar(@Valid @RequestBody GeneroRequestDTO dto) {
        GeneroResponseDTO criado = generoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Lista todos os gêneros cadastrados com suporte a paginação global.
     *
     * @param pageable Parâmetros de página e tamanho fornecidos pela requisição HTTP.
     * @return HTTP 200 OK contendo a página de gêneros.
     */
    @GetMapping
    public ResponseEntity<Page<GeneroResponseDTO>> listar(Pageable pageable) {
        Page<GeneroResponseDTO> pagina = generoService.listar(pageable);
        return ResponseEntity.ok(pagina);
    }

    /**
     * Busca um gênero específico através de seu identificador UUID.
     *
     * @param id Identificador UUID informado no caminho da URL.
     * @return HTTP 200 OK com o gênero localizado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GeneroResponseDTO> buscarPorId(@PathVariable String id) {
        GeneroResponseDTO genero = generoService.buscarPorId(id);
        return ResponseEntity.ok(genero);
    }

    /**
     * Atualiza as informações de um gênero literário existente.
     *
     * @param id Identificador UUID do gênero a ser editado.
     * @param dto Corpo JSON validado com os novos dados.
     * @return HTTP 200 OK com os dados atualizados.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GeneroResponseDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody GeneroRequestDTO dto
    ) {
        GeneroResponseDTO atualizado = generoService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Exclui fisicamente um gênero do sistema.
     *
     * @param id Identificador UUID do gênero a ser excluído.
     * @return HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        generoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
