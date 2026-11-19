package com.br.The_Reading_Nest_Back.controllers;

import com.br.The_Reading_Nest_Back.dtos.LivroRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.LivroResponseDTO;
import com.br.The_Reading_Nest_Back.entities.Livro;
import com.br.The_Reading_Nest_Back.entities.StatusLivro;
import com.br.The_Reading_Nest_Back.mappers.LivroMapper;
import com.br.The_Reading_Nest_Back.services.LivroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Explicação simples:
 * Ponto de comunicação para tudo relacionado ao catálogo de livros. Permite ao frontend buscar a lista
 * de livros (podendo filtrar por categoria literária ou se o livro está ativo), abrir detalhes de um exemplar,
 * cadastrar novos livros no acervo, atualizar dados e desativar títulos do catálogo, utilizando o LivroMapper
 * para transformar as entidades retornadas pelo serviço em DTOs de resposta.
 *
 * Explicação técnica:
 * Controlador RESTful mapeado em '/livros'. Suporta filtragem flexível via query parameters opcionais
 * ('status' e 'generoId'), paginação integrada e padronizada via Pageable do Spring Data, validação
 * automática dos payloads através da anotação @Valid e projeção de entidades Livro em LivroResponseDTO
 * através de injeção de dependência do LivroMapper.
 */
@RestController
@RequestMapping("/livros")
public class LivroController {

    private final LivroService livroService;
    private final LivroMapper livroMapper;

    public LivroController(LivroService livroService, LivroMapper livroMapper) {
        this.livroService = livroService;
        this.livroMapper = livroMapper;
    }

    /**
     * Cadastra um novo livro no catálogo da biblioteca.
     *
     * @param dto Formulário validado com informações cadastrais do livro.
     * @return HTTP 201 Created contendo o livro cadastrado convertido via Mapper.
     */
    @PostMapping
    public ResponseEntity<LivroResponseDTO> criar(@Valid @RequestBody LivroRequestDTO dto) {
        Livro criado = livroService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(livroMapper.toResponseDTO(criado));
    }

    /**
     * Consulta livros de maneira paginada permitindo filtros opcionais por status e gênero.
     *
     * @param status Filtro opcional por status do livro ('ativo' ou 'desativado').
     * @param generoId Filtro opcional por ID do gênero literário.
     * @param pageable Configuração global de paginação.
     * @return HTTP 200 OK com a página de livros convertida via Mapper.
     */
    @GetMapping
    public ResponseEntity<Page<LivroResponseDTO>> listar(
            @RequestParam(required = false) StatusLivro status,
            @RequestParam(required = false) String generoId,
            Pageable pageable
    ) {
        Page<Livro> pagina = livroService.listar(status, generoId, pageable);
        return ResponseEntity.ok(pagina.map(livroMapper::toResponseDTO));
    }

    /**
     * Recupera os detalhes completos de um livro pelo seu identificador UUID.
     *
     * @param id Identificador do livro na rota.
     * @return HTTP 200 OK com os detalhes da obra convertidos via Mapper.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LivroResponseDTO> buscarPorId(@PathVariable String id) {
        Livro livro = livroService.buscarPorId(id);
        return ResponseEntity.ok(livroMapper.toResponseDTO(livro));
    }

    /**
     * Atualiza as informações completas de um livro existente no acervo.
     *
     * @param id Identificador do livro a ser atualizado.
     * @param dto Novos dados para a obra literária.
     * @return HTTP 200 OK com o livro atualizado convertido via Mapper.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LivroResponseDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody LivroRequestDTO dto
    ) {
        Livro atualizado = livroService.atualizar(id, dto);
        return ResponseEntity.ok(livroMapper.toResponseDTO(atualizado));
    }

    /**
     * Desativa um livro do catálogo (soft-delete), tornando-o indisponível para novos aluguéis.
     *
     * @param id Identificador UUID do livro.
     * @return HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        livroService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
