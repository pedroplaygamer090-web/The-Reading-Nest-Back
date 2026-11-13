package com.br.The_Reading_Nest_Back.controllers;

import com.br.The_Reading_Nest_Back.dtos.*;
import com.br.The_Reading_Nest_Back.services.AluguelService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Explicação simples:
 * Ponto central de atendimento para os empréstimos de livros. Permite ao leitor alugar livros,
 * ao administrador visualizar os aluguéis pendentes e atrasados, e processar a devolução ou
 * o cancelamento de um aluguel com reposição imediata dos livros ao estoque.
 *
 * Explicação técnica:
 * Controlador RESTful mapeado em '/alugueis'. Expõe rotas para ciclo de vida de empréstimos,
 * incluindo criação atômica, listagem paginada enxuta de não concluídos (evitando queries N+1),
 * consulta profunda de subitens ('detalhes_alugueis') e ações transacionais de devolução e cancelamento.
 */
@RestController
@RequestMapping("/alugueis")
public class AluguelController {

    private final AluguelService aluguelService;

    public AluguelController(AluguelService aluguelService) {
        this.aluguelService = aluguelService;
    }

    /**
     * Realiza um novo empréstimo de livros, validando usuário, acervo e estoque disponível.
     *
     * @param dto Dados da requisição contendo usuário, prazo e lista de livros.
     * @return HTTP 201 Created com os dados detalhados do empréstimo realizado.
     */
    @PostMapping
    public ResponseEntity<AluguelDetalhadoResponseDTO> criar(@Valid @RequestBody AluguelRequestDTO dto) {
        AluguelDetalhadoResponseDTO criado = aluguelService.criarAluguel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    /**
     * Lista os aluguéis não concluídos ('ativo' e 'atrasado') ordenados prioritariamente por urgência de prazo.
     *
     * @param pageable Configuração de paginação.
     * @return HTTP 200 OK com a página de resumos de aluguéis.
     */
    @GetMapping
    public ResponseEntity<Page<AluguelResumoResponseDTO>> listarNaoConcluidos(Pageable pageable) {
        Page<AluguelResumoResponseDTO> pagina = aluguelService.listarNaoConcluidos(pageable);
        return ResponseEntity.ok(pagina);
    }

    /**
     * Consulta os dados detalhados de um aluguel específico trazendo a lista completa de livros.
     *
     * @param id Identificador UUID do aluguel.
     * @return HTTP 200 OK com o aluguel detalhado.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AluguelDetalhadoResponseDTO> buscarPorId(@PathVariable String id) {
        AluguelDetalhadoResponseDTO aluguel = aluguelService.buscarDetalhado(id);
        return ResponseEntity.ok(aluguel);
    }

    /**
     * Atualiza o prazo de devolução e/ou status de um aluguel existente.
     *
     * @param id Identificador UUID do aluguel.
     * @param dto Novos parâmetros para o aluguel.
     * @return HTTP 200 OK com o aluguel atualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AluguelDetalhadoResponseDTO> atualizar(
            @PathVariable String id,
            @RequestBody AluguelUpdateDTO dto
    ) {
        AluguelDetalhadoResponseDTO atualizado = aluguelService.atualizarAluguel(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Finaliza um aluguel registrando a devolução dos livros e incrementando o estoque físico disponível.
     *
     * @param id Identificador UUID do aluguel a ser devolvido.
     * @return HTTP 200 OK com o aluguel finalizado.
     */
    @PostMapping("/{id}/devolver")
    public ResponseEntity<AluguelDetalhadoResponseDTO> devolver(@PathVariable String id) {
        AluguelDetalhadoResponseDTO devolvido = aluguelService.devolverAluguel(id);
        return ResponseEntity.ok(devolvido);
    }

    /**
     * Cancela um aluguel, repondo os livros ao estoque e registrando eventual justificativa.
     *
     * @param id Identificador UUID do aluguel.
     * @param dto Justificativa opcional para o cancelamento.
     * @return HTTP 200 OK com o aluguel cancelado.
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<AluguelDetalhadoResponseDTO> cancelar(
            @PathVariable String id,
            @RequestBody(required = false) AluguelCancelamentoDTO dto
    ) {
        AluguelDetalhadoResponseDTO cancelado = aluguelService.cancelarAluguel(id, dto);
        return ResponseEntity.ok(cancelado);
    }
}
