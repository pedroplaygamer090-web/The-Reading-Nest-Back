package com.br.The_Reading_Nest_Back.controllers;

import com.br.The_Reading_Nest_Back.dtos.AluguelCancelamentoDTO;
import com.br.The_Reading_Nest_Back.dtos.AluguelDetalhadoResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.AluguelRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.AluguelResumoResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.AluguelUpdateDTO;
import com.br.The_Reading_Nest_Back.entities.Aluguel;
import com.br.The_Reading_Nest_Back.entities.AluguelCompleto;
import com.br.The_Reading_Nest_Back.mappers.AluguelMapper;
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
 * o cancelamento de um aluguel, utilizando o AluguelMapper para converter os agregados de entidades
 * nos DTOs de resposta da API.
 *
 * Explicação técnica:
 * Controlador RESTful mapeado em '/alugueis'. Expõe rotas para ciclo de vida de empréstimos,
 * delegando as operações para o AluguelService e projetando os agregados AluguelCompleto e entidades Aluguel
 * nos DTOs AluguelDetalhadoResponseDTO e AluguelResumoResponseDTO através do AluguelMapper.
 */
@RestController
@RequestMapping("/alugueis")
public class AluguelController {

    private final AluguelService aluguelService;
    private final AluguelMapper aluguelMapper;

    public AluguelController(AluguelService aluguelService, AluguelMapper aluguelMapper) {
        this.aluguelService = aluguelService;
        this.aluguelMapper = aluguelMapper;
    }

    /**
     * Realiza um novo empréstimo de livros, validando usuário, acervo e estoque disponível.
     *
     * @param dto Dados da requisição contendo usuário, prazo e lista de livros.
     * @return HTTP 201 Created com os dados detalhados do empréstimo convertidos via Mapper.
     */
    @PostMapping
    public ResponseEntity<AluguelDetalhadoResponseDTO> criar(@Valid @RequestBody AluguelRequestDTO dto) {
        AluguelCompleto criado = aluguelService.criarAluguel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(aluguelMapper.toDetalhadoDTO(criado));
    }

    /**
     * Lista os aluguéis não concluídos ('ativo' e 'atrasado') ordenados prioritariamente por urgência de prazo.
     *
     * @param pageable Configuração de paginação.
     * @return HTTP 200 OK com a página de resumos de aluguéis convertida via Mapper.
     */
    @GetMapping
    public ResponseEntity<Page<AluguelResumoResponseDTO>> listarNaoConcluidos(Pageable pageable) {
        Page<Aluguel> pagina = aluguelService.listarNaoConcluidos(pageable);
        return ResponseEntity.ok(pagina.map(aluguelMapper::toResumoDTO));
    }

    /**
     * Consulta os dados detalhados de um aluguel específico trazendo a lista completa de livros.
     *
     * @param id Identificador UUID do aluguel.
     * @return HTTP 200 OK com o aluguel detalhado convertido via Mapper.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AluguelDetalhadoResponseDTO> buscarPorId(@PathVariable String id) {
        AluguelCompleto aluguel = aluguelService.buscarDetalhado(id);
        return ResponseEntity.ok(aluguelMapper.toDetalhadoDTO(aluguel));
    }

    /**
     * Atualiza o prazo de devolução e/ou status de um aluguel existente.
     *
     * @param id Identificador UUID do aluguel.
     * @param dto Novos parâmetros para o aluguel.
     * @return HTTP 200 OK com o aluguel atualizado convertido via Mapper.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AluguelDetalhadoResponseDTO> atualizar(
            @PathVariable String id,
            @Valid @RequestBody AluguelUpdateDTO dto
    ) {
        AluguelCompleto atualizado = aluguelService.atualizarAluguel(id, dto);
        return ResponseEntity.ok(aluguelMapper.toDetalhadoDTO(atualizado));
    }

    /**
     * Finaliza um aluguel registrando a devolução dos livros e incrementando o estoque físico disponível.
     *
     * @param id Identificador UUID do aluguel a ser devolvido.
     * @return HTTP 200 OK com o aluguel finalizado convertido via Mapper.
     */
    @PostMapping("/{id}/devolver")
    public ResponseEntity<AluguelDetalhadoResponseDTO> devolver(@PathVariable String id) {
        AluguelCompleto devolvido = aluguelService.devolverAluguel(id);
        return ResponseEntity.ok(aluguelMapper.toDetalhadoDTO(devolvido));
    }

    /**
     * Cancela um aluguel, repondo os livros ao estoque e registrando eventual justificativa.
     *
     * @param id Identificador UUID do aluguel.
     * @param dto Justificativa opcional para o cancelamento.
     * @return HTTP 200 OK com o aluguel cancelado convertido via Mapper.
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<AluguelDetalhadoResponseDTO> cancelar(
            @PathVariable String id,
            @RequestBody(required = false) AluguelCancelamentoDTO dto
    ) {
        AluguelCompleto cancelado = aluguelService.cancelarAluguel(id, dto);
        return ResponseEntity.ok(aluguelMapper.toDetalhadoDTO(cancelado));
    }
}
