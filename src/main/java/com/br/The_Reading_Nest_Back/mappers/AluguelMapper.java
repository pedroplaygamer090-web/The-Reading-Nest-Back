package com.br.The_Reading_Nest_Back.mappers;

import com.br.The_Reading_Nest_Back.dtos.AluguelRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.AluguelResumoResponseDTO;
import com.br.The_Reading_Nest_Back.entities.Aluguel;
import com.br.The_Reading_Nest_Back.entities.DetalheAluguel;
import com.br.The_Reading_Nest_Back.entities.StatusAluguel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Explicação simples:
 * Faz a conversão dos pedidos de aluguel e dos livros emprestados em registros prontos para o banco,
 * definindo a data e hora do aluguel e iniciando o empréstimo com a situação de 'ativo'.
 *
 * Explicação técnica:
 * Componente Spring gerenciado (@Component) que monta a entidade Aluguel com carimbo de tempo
 * atual (LocalDateTime.now()), status operacional inicial 'ativo' e constrói as entidades filhas
 * associativas 'DetalheAluguel' relacionando o cabeçalho do empréstimo a cada exemplar retirado.
 */
@Component
public class AluguelMapper {

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
     * Converte a entidade Aluguel agregada com o nome do usuário em DTO de resumo.
     *
     * @param aluguel Entidade Aluguel.
     * @param usuarioNome Nome do usuário leitor.
     * @return DTO resumido para listagens.
     */
    public AluguelResumoResponseDTO toResumoDTO(Aluguel aluguel, String usuarioNome) {
        return new AluguelResumoResponseDTO(
                aluguel.id(),
                aluguel.usuariosId(),
                usuarioNome,
                aluguel.dataAluguel(),
                aluguel.dataDevolucaoPrevista(),
                aluguel.status()
        );
    }
}
