package com.br.The_Reading_Nest_Back.mappers;

import com.br.The_Reading_Nest_Back.dtos.GeneroRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.GeneroResponseDTO;
import com.br.The_Reading_Nest_Back.entities.Genero;
import org.springframework.stereotype.Component;

/**
 * Explicação simples:
 * Um tradutor que transforma os dados que vêm da internet (DTOs) na entidade interna de gênero da biblioteca,
 * e transforma a entidade interna no formato de resposta que a internet e o frontend esperam receber.
 *
 * Explicação técnica:
 * Componente Spring gerenciado (@Component) encarregado da conversão bidirecional entre DTOs e entidades
 * de domínio. Isola as camadas e garante que os contratos HTTP e a estrutura do banco não fiquem acoplados.
 */
@Component
public class GeneroMapper {

    /**
     * Converte o DTO de requisição na entidade Genero recebendo o UUID gerado.
     *
     * @param id Identificador universal UUID gerado na camada de serviço.
     * @param dto Dados validados recebidos na requisição HTTP.
     * @return Instância imutável da entidade Genero.
     */
    public Genero toEntity(String id, GeneroRequestDTO dto) {
        return new Genero(id, dto.nome().trim());
    }

    /**
     * Converte a entidade de domínio Genero em DTO de resposta.
     *
     * @param entity Entidade recuperada do repositório.
     * @return DTO serializável para JSON.
     */
    public GeneroResponseDTO toResponseDTO(Genero entity) {
        return new GeneroResponseDTO(entity.id(), entity.nome());
    }
}
