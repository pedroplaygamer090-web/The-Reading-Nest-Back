package com.br.The_Reading_Nest_Back.services;

import com.br.The_Reading_Nest_Back.dtos.*;
import com.br.The_Reading_Nest_Back.entities.*;
import com.br.The_Reading_Nest_Back.exceptions.CustomException;
import com.br.The_Reading_Nest_Back.mappers.AluguelMapper;
import com.br.The_Reading_Nest_Back.repositories.AluguelRepository;
import com.br.The_Reading_Nest_Back.repositories.LivroRepository;
import com.br.The_Reading_Nest_Back.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Explicação simples:
 * O cérebro das operações de aluguel e empréstimo de livros. Ele confere se o leitor está com a conta ativa,
 * se há livros suficientes na estante antes de permitir a retirada, diminui o estoque no momento do empréstimo
 * e devolve os livros para a estante física quando o aluguel é concluído ou cancelado.
 *
 * Explicação técnica:
 * Camada de serviço responsável pelo fluxo de ciclo de vida de aluguéis.
 * Assegura atomicidade com anotações @Transactional (rollback automático caso qualquer validação
 * ou decremento falhe), executa controle de concorrência com decremento atômico condicional no MySQL
 * (evitando que o estoque fique negativo), valida integridade cadastral e gerencia o incremento de volta
 * ao acervo físico em devoluções e cancelamentos.
 */
@Service
public class AluguelService {

    private static final Logger log = LoggerFactory.getLogger(AluguelService.class);

    private final AluguelRepository aluguelRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final AluguelMapper aluguelMapper;

    public AluguelService(
            AluguelRepository aluguelRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository,
            AluguelMapper aluguelMapper
    ) {
        this.aluguelRepository = aluguelRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.aluguelMapper = aluguelMapper;
    }

    /**
     * Cria um novo aluguel de forma atômica, validando leitor, livros e decrementando o estoque físico.
     *
     * @param dto Dados do empréstimo contendo usuário, data de entrega e lista de IDs dos livros.
     * @return DTO com a visão detalhada do aluguel recém-criado e seus livros vinculados.
     */
    @Transactional
    public AluguelDetalhadoResponseDTO criarAluguel(AluguelRequestDTO dto) {
        // 1. Validação de livros duplicados na própria requisição
        Set<String> livrosUnicos = new HashSet<>(dto.livrosIds());
        if (livrosUnicos.size() < dto.livrosIds().size()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "A LISTA DE LIVROS NÃO PODE CONTER LIVROS DUPLICADOS");
        }

        // 2. Validação do usuário
        Usuario usuario = usuarioRepository.findEntityById(dto.usuarioId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "USUÁRIO NÃO ENCONTRADO"));

        if (usuario.statusConta() != StatusConta.ativo) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "USUÁRIO INATIVO OU SUSPENSO NÃO PODE REALIZAR ALUGUEL");
        }

        // 3. Validação de cada livro e decremento atômico de estoque
        for (String livroId : dto.livrosIds()) {
            Livro livro = livroRepository.findEntityById(livroId)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "LIVRO NÃO ENCONTRADO"));

            if (livro.statusLivro() != StatusLivro.ativo) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "O LIVRO '" + livro.titulo() + "' ESTÁ DESATIVADO PARA ALUGUEL");
            }

            int rowsAffected = livroRepository.decrementarEstoque(livroId);
            if (rowsAffected == 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "ESTOQUE ESGOTADO PARA O LIVRO '" + livro.titulo() + "'");
            }
        }

        // 4. Persistência do cabeçalho do aluguel e dos detalhes associativos
        String aluguelId = UUID.randomUUID().toString();
        Aluguel aluguel = aluguelMapper.toEntity(aluguelId, dto);
        aluguelRepository.insertAluguel(aluguel);

        for (String livroId : dto.livrosIds()) {
            String detalheId = UUID.randomUUID().toString();
            DetalheAluguel detalhe = aluguelMapper.toDetalheEntity(detalheId, aluguelId, livroId);
            aluguelRepository.insertDetalheAluguel(detalhe);
        }

        return buscarDetalhado(aluguelId);
    }

    /**
     * Retorna a listagem paginada de aluguéis não concluídos ('ativo' e 'atrasado') para o administrador.
     *
     * @param pageable Configurações de paginação do Spring Data.
     * @return Página de AluguelResumoResponseDTO ordenada por urgência de entrega.
     */
    public Page<AluguelResumoResponseDTO> listarNaoConcluidos(Pageable pageable) {
        return aluguelRepository.findNaoConcluidos(pageable);
    }

    /**
     * Retorna o histórico paginado de aluguéis de um cliente específico (tela Minha Conta).
     *
     * @param usuarioId Identificador UUID do leitor.
     * @param pageable Configurações de paginação.
     * @return Página de AluguelResumoResponseDTO do usuário.
     */
    public Page<AluguelResumoResponseDTO> listarPorUsuario(String usuarioId, Pageable pageable) {
        if (usuarioRepository.findById(usuarioId).isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "USUÁRIO NÃO ENCONTRADO");
        }

        return aluguelRepository.findByUsuarioId(usuarioId, pageable);
    }

    /**
     * Busca os dados detalhados de um aluguel contendo a lista completa de livros emprestados.
     *
     * @param id Identificador UUID do aluguel.
     * @return DTO com informações detalhadas do aluguel e dos livros.
     */
    public AluguelDetalhadoResponseDTO buscarDetalhado(String id) {
        return aluguelRepository.findByIdComLivros(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "ALUGUEL NÃO ENCONTRADO"));
    }

    /**
     * Atualiza dados de um aluguel (prazo ou status), devolvendo o estoque caso mude para 'concluido'.
     *
     * @param id Identificador UUID do aluguel.
     * @param dto Novos parâmetros para o aluguel.
     * @return DTO detalhado atualizado.
     */
    @Transactional
    public AluguelDetalhadoResponseDTO atualizarAluguel(String id, AluguelUpdateDTO dto) {
        Aluguel aluguelAtual = aluguelRepository.findEntityById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "ALUGUEL NÃO ENCONTRADO"));

        LocalDate dataFinal = dto.dataDevolucaoPrevista() != null ? dto.dataDevolucaoPrevista() : aluguelAtual.dataDevolucaoPrevista();
        StatusAluguel statusFinal = dto.status() != null ? dto.status() : aluguelAtual.status();

        // Se transitou de não-concluído para concluído, repõe o estoque de cada livro
        if (aluguelAtual.status() != StatusAluguel.concluido && statusFinal == StatusAluguel.concluido) {
            List<String> livrosIds = aluguelRepository.findLivrosIdsByAluguelId(id);
            for (String livroId : livrosIds) {
                livroRepository.incrementarEstoque(livroId);
            }
        }

        aluguelRepository.updateAluguel(id, dataFinal, statusFinal);

        return buscarDetalhado(id);
    }

    /**
     * Registra a devolução dos livros de um aluguel, marcando-o como concluído e repondo os itens ao estoque.
     *
     * @param id Identificador UUID do aluguel a ser finalizado.
     * @return DTO detalhado com status concluído.
     */
    @Transactional
    public AluguelDetalhadoResponseDTO devolverAluguel(String id) {
        Aluguel aluguel = aluguelRepository.findEntityById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "ALUGUEL NÃO ENCONTRADO"));

        if (aluguel.status() == StatusAluguel.concluido) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "ESTE ALUGUEL JÁ FOI CONCLUÍDO ANTERIORMENTE");
        }

        aluguelRepository.updateAluguel(id, aluguel.dataDevolucaoPrevista(), StatusAluguel.concluido);

        List<String> livrosIds = aluguelRepository.findLivrosIdsByAluguelId(id);
        for (String livroId : livrosIds) {
            livroRepository.incrementarEstoque(livroId);
        }

        return buscarDetalhado(id);
    }

    /**
     * Cancela um aluguel em andamento, devolvendo os livros ao acervo e registrando eventual justificativa.
     *
     * @param id Identificador UUID do aluguel a ser cancelado.
     * @param dto Justificativa opcional fornecida pelo usuário ou administrador.
     * @return DTO detalhado atualizado.
     */
    @Transactional
    public AluguelDetalhadoResponseDTO cancelarAluguel(String id, AluguelCancelamentoDTO dto) {
        Aluguel aluguel = aluguelRepository.findEntityById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "ALUGUEL NÃO ENCONTRADO"));

        if (aluguel.status() == StatusAluguel.concluido) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "ALUGUEL JÁ CONCLUÍDO NÃO PODE SER CANCELADO");
        }

        aluguelRepository.updateAluguel(id, aluguel.dataDevolucaoPrevista(), StatusAluguel.concluido);

        List<String> livrosIds = aluguelRepository.findLivrosIdsByAluguelId(id);
        for (String livroId : livrosIds) {
            livroRepository.incrementarEstoque(livroId);
        }

        if (dto != null && dto.motivo() != null && !dto.motivo().isBlank()) {
            log.info("Aluguel ID '{}' cancelado com justificativa: '{}'", id, dto.motivo().trim());
        } else {
            log.info("Aluguel ID '{}' cancelado sem justificativa informada.", id);
        }

        return buscarDetalhado(id);
    }
}
