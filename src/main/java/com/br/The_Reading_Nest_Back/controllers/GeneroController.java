package com.br.The_Reading_Nest_Back.controllers;

import com.br.The_Reading_Nest_Back.dtos.GeneroRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.GeneroResponseDTO;
import com.br.The_Reading_Nest_Back.entities.Genero;
import com.br.The_Reading_Nest_Back.exceptions.CustomException;
import com.br.The_Reading_Nest_Back.mappers.GeneroMapper;
import com.br.The_Reading_Nest_Back.security.SecurityUtils;
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
 * enviados pelo navegador (como criar um gênero, listar todas as categorias, alterar ou apagar),
 * repassa para o serviço executar as regras de negócio com entidades e utiliza o GeneroMapper
 * para transformar os dados retornados em DTOs antes de responder ao cliente.
 *
 * Explicação técnica:
 * Controlador REST (@RestController) mapeado no plural para a rota '/generos' conforme padrão RESTful.
 * Valida os dados de entrada com @Valid disparando automaticamente o Bean Validation, delega
 * a execução para o 'GeneroService' e utiliza o 'GeneroMapper' para projetar as entidades de domínio 'Genero'
 * nos DTOs de resposta 'GeneroResponseDTO'.
 */
@RestController
@RequestMapping("/generos")
public class GeneroController {

    private final GeneroService generoService;
    private final GeneroMapper generoMapper;
    private final SecurityUtils securityUtils;

    public GeneroController(GeneroService generoService, GeneroMapper generoMapper, SecurityUtils securityUtils) {
        this.generoService = generoService;
        this.generoMapper = generoMapper;
        this.securityUtils = securityUtils;
    }

    /**
     * Cria um novo gênero literário na biblioteca.
     *
     * @param dto Corpo JSON validado contendo o nome da categoria.
     * @return HTTP 201 Created com os dados do gênero criado formatados via Mapper.
     */
    @PostMapping
    public ResponseEntity<GeneroResponseDTO> criar(@Valid @RequestBody GeneroRequestDTO dto) {
if(!securityUtils.isAdmin()) {
    throw new CustomException(HttpStatus.FORBIDDEN, "APENAS ADMINISTRADORES PODEM CRIAR GÊNEROS");
}
        // pega o role e se não for admin, dá
        // erro
        Genero criado = generoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(generoMapper.toResponseDTO(criado));
    }

    /**
     * Lista todos os gêneros cadastrados com suporte a paginação global.
     *
     * @param pageable Parâmetros de página e tamanho fornecidos pela requisição HTTP.
     * @return HTTP 200 OK contendo a página de gêneros convertida via Mapper.
     */
    @GetMapping
    public ResponseEntity<Page<GeneroResponseDTO>> listar(Pageable pageable) {
        Page<Genero> pagina = generoService.listar(pageable);
        return ResponseEntity.ok(pagina.map(generoMapper::toResponseDTO));
    }

    /**
     * Busca um gênero específico através de seu identificador UUID.
     *
     * @param id Identificador UUID informado no caminho da URL.
     * @return HTTP 200 OK com o gênero localizado convertido via Mapper.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GeneroResponseDTO> buscarPorId(@PathVariable String id) {
        Genero genero = generoService.buscarPorId(id);
        return ResponseEntity.ok(generoMapper.toResponseDTO(genero));
    }

    /**
     * Atualiza as informações de um gênero literário existente.
     *
     * @param id Identificador UUID do gênero a ser editado.
     * @param dto Corpo JSON validado com os novos dados.
     * @return HTTP 200 OK com os dados atualizados convertidos via Mapper.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GeneroResponseDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody GeneroRequestDTO dto
    ) {
        Genero atualizado = generoService.atualizar(id, dto);
        return ResponseEntity.ok(generoMapper.toResponseDTO(atualizado));
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
