package com.br.The_Reading_Nest_Back.mappers;

import com.br.The_Reading_Nest_Back.dtos.AluguelDetalhadoResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.AluguelRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.AluguelResumoResponseDTO;
import com.br.The_Reading_Nest_Back.dtos.LivroResponseDTO;
import com.br.The_Reading_Nest_Back.entities.Aluguel;
import com.br.The_Reading_Nest_Back.entities.AluguelCompleto;
import com.br.The_Reading_Nest_Back.entities.DetalheAluguel;
import com.br.The_Reading_Nest_Back.entities.Livro;
import com.br.The_Reading_Nest_Back.entities.StatusAluguel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Explicação simples:
 * Faz a conversão dos pedidos de aluguel e dos livros emprestados em registros prontos para o banco,
 * e transforma os dados de aluguel armazenados nos formatos de resposta (resumido ou detalhado) para o navegador.
 *
 * Explicação técnica:
 * Componente Spring gerenciado (@Component) que centraliza todas as transformações bidirecionais
 * entre o domínio de aluguéis (Aluguel, DetalheAluguel, AluguelCompleto) e os DTOs de transporte da API
 * (AluguelRequestDTO, AluguelResumoResponseDTO, AluguelDetalhadoResponseDTO).
 */
@Component
public class AluguelMapper {

    private final LivroMapper livroMapper;

    public AluguelMapper(LivroMapper livroMapper) {
        this.livroMapper = livroMapper;
    }

    /**
     * Converte os dados do formulário de empréstimo na entidade Aluguel.
     *
     * @param id Identificador UUID do aluguel gerado no Service.
     * @param dto DTO contendo o ID do usuário e a data prevista de entrega.
     * @return Entidade Aluguel configurada com status ativo e data atual.
     */
    public Aluguel toEntity(String id, AluguelRequestDTO dto) {
        return new Aluguel(
                id,
                dto.usuarioId(),
                null,
                LocalDateTime.now(),
                dto.dataDevolucaoPrevista(),
                StatusAluguel.ativo
        );
    }

    /**
     * Constrói uma entidade de relacionamento 'DetalheAluguel' vinculando o aluguel ao livro.
     *
     * @param id Identificador UUID do registro de detalhe.
     * @param aluguelId UUID do aluguel principal.
     * @param livroId UUID do livro emprestado.
     * @return Entidade DetalheAluguel.
     */
    public DetalheAluguel toDetalheEntity(String id, String aluguelId, String livroId) {
        return new DetalheAluguel(id, aluguelId, livroId);
    }

    /**
     * Converte a entidade Aluguel no DTO de resumo para listagens públicas.
     *
     * @param aluguel Entidade Aluguel (contendo usuarioNome via join).
     * @return DTO resumido para listagens.
     */
    public AluguelResumoResponseDTO toResumoDTO(Aluguel aluguel) {
        return new AluguelResumoResponseDTO(
                aluguel.id(),
                aluguel.usuariosId(),
                aluguel.usuarioNome(),
                aluguel.dataAluguel(),
                aluguel.dataDevolucaoPrevista(),
                aluguel.status()
        );
    }

    /**
     * Converte a entidade Aluguel agregada com o nome explícito do usuário em DTO de resumo.
     *
     * @param aluguel Entidade Aluguel.
     * @param usuarioNome Nome do usuário leitor.
     * @return DTO resumido para listagens.
     */
    public AluguelResumoResponseDTO toResumoDTO(Aluguel aluguel, String usuarioNome) {
        return new AluguelResumoResponseDTO(
                aluguel.id(),
                aluguel.usuariosId(),
                usuarioNome != null ? usuarioNome : aluguel.usuarioNome(),
                aluguel.dataAluguel(),
                aluguel.dataDevolucaoPrevista(),
                aluguel.status()
        );
    }

    /**
     * Converte a entidade Aluguel e a lista de entidades Livro no DTO detalhado de saída.
     *
     * @param aluguel Entidade Aluguel.
     * @param livros Lista de entidades de livros associados.
     * @return DTO detalhado com cabeçalho e livros.
     */
    public AluguelDetalhadoResponseDTO toDetalhadoDTO(Aluguel aluguel, List<Livro> livros) {
        List<LivroResponseDTO> livrosDTO = livros.stream()
                .map(livroMapper::toResponseDTO)
                .toList();

        return new AluguelDetalhadoResponseDTO(
                aluguel.id(),
                aluguel.usuariosId(),
                aluguel.usuarioNome(),
                aluguel.dataAluguel(),
                aluguel.dataDevolucaoPrevista(),
                aluguel.status(),
                livrosDTO
        );
    }

    /**
     * Converte o objeto de domínio agregado AluguelCompleto em DTO detalhado.
     *
     * @param completo Agregado contendo o cabeçalho e a lista de livros.
     * @return DTO detalhado com cabeçalho e livros.
     */
    public AluguelDetalhadoResponseDTO toDetalhadoDTO(AluguelCompleto completo) {
        return toDetalhadoDTO(completo.aluguel(), completo.livros());
    }
}
