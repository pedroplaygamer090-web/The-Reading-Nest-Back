package com.br.The_Reading_Nest_Back.mappers;

import com.br.The_Reading_Nest_Back.dtos.LivroRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.LivroResponseDTO;
import com.br.The_Reading_Nest_Back.entities.Livro;
import com.br.The_Reading_Nest_Back.entities.StatusLivro;
import org.springframework.stereotype.Component;

/**
 * Explicação simples:
 * Tradutor responsável por converter os dados de cadastro e edição de livros enviados pelo usuário
 * na entidade Livro, bem como empacotar os dados do livro para envio de volta ao navegador.
 *
 * Explicação técnica:
 * Componente anotado com @Component responsável pelo mapeamento de dados entre LivroRequestDTO,
 * Livro (Record) e LivroResponseDTO. Define fallback padrão para 'statusLivro' (ativo caso nulo)
 * e higieniza strings textuais para persistência consistente no banco.
 */
@Component
public class LivroMapper {

    /**
     * Converte o formulário de livro na entidade Livro recebendo o UUID gerado.
     *
     * @param id Identificador UUID gerado pelo Service.
     * @param dto Dados da requisição validados no Controller.
     * @return Entidade Livro pronta para inserção ou atualização no banco.
     */
    public Livro toEntity(String id, LivroRequestDTO dto) {
        StatusLivro status = dto.statusLivro() != null ? dto.statusLivro() : StatusLivro.ativo;
        return new Livro(
                id,
                dto.generoId(),
                dto.titulo().trim(),
                dto.autor().trim(),
                dto.descricao() != null ? dto.descricao().trim() : null,
                dto.anoPublicacao(),
                dto.isbn() != null && !dto.isbn().isBlank() ? dto.isbn().trim() : null,
                dto.quantidadeDisponivel(),
                status
        );
    }

    /**
     * Converte a entidade interna Livro juntamente com o nome textual do gênero no DTO de resposta.
     *
     * @param entity Entidade Livro vinda do banco.
     * @param generoNome Nome textual do gênero literário correspondente.
     * @return DTO serializado para o frontend.
     */
    public LivroResponseDTO toResponseDTO(Livro entity, String generoNome) {
        return new LivroResponseDTO(
                entity.id(),
                entity.generosId(),
                generoNome,
                entity.titulo(),
                entity.autor(),
                entity.descricao(),
                entity.anoPublicacao(),
                entity.isbn(),
                entity.quantidadeDisponivel(),
                entity.statusLivro()
        );
    }
}
