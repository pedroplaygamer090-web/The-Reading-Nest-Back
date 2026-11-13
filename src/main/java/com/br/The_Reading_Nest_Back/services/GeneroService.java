package com.br.The_Reading_Nest_Back.services;

import com.br.The_Reading_Nest_Back.dtos.GeneroRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.GeneroResponseDTO;
import com.br.The_Reading_Nest_Back.entities.Genero;
import com.br.The_Reading_Nest_Back.exceptions.CustomException;
import com.br.The_Reading_Nest_Back.mappers.GeneroMapper;
import com.br.The_Reading_Nest_Back.repositories.GeneroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Explicação simples:
 * O cérebro das regras para gêneros literários. Ele cuida para que nenhum gênero com nome repetido
 * seja cadastrado, gera um código único (UUID) para cada categoria nova, busca categorias de forma
 * organizada e garante que alterações ou exclusões só ocorram se o gênero realmente existir.
 *
 * Explicação técnica:
 * Camada de serviço anotada com @Service que encapsula a lógica de negócio do domínio 'Genero'.
 * Coordena a validação de unicidade de chave de negócio ('nome'), a geração de UUIDs versão 4
 * antes da inserção física no MySQL, o controle transacional com @Transactional e o mapeamento DTO-Entidade.
 */
@Service
public class GeneroService {

    private final GeneroRepository generoRepository;
    private final GeneroMapper generoMapper;

    public GeneroService(GeneroRepository generoRepository, GeneroMapper generoMapper) {
        this.generoRepository = generoRepository;
        this.generoMapper = generoMapper;
    }

    /**
     * Cadastra um novo gênero literário após validar a ausência de nomes duplicados.
     *
     * @param dto Dados validados contendo o nome pretendido para a categoria.
     * @return DTO com os dados do gênero recém-criado e seu novo UUID.
     */
    @Transactional
    public GeneroResponseDTO criar(GeneroRequestDTO dto) {
        if (generoRepository.existsByNome(dto.nome().trim())) {
            throw new CustomException(HttpStatus.CONFLICT, "JÁ EXISTE UM GÊNERO COM ESTE NOME");
        }

        String id = UUID.randomUUID().toString();
        Genero genero = generoMapper.toEntity(id, dto);
        generoRepository.insert(genero);

        return generoMapper.toResponseDTO(genero);
    }

    /**
     * Recupera a listagem paginada de todos os gêneros literários em ordem alfabética.
     *
     * @param pageable Configurações de número da página e tamanho.
     * @return Página de GeneroResponseDTO.
     */
    public Page<GeneroResponseDTO> listar(Pageable pageable) {
        return generoRepository.findAll(pageable);
    }

    /**
     * Busca um gênero literário pelo seu identificador UUID universal.
     *
     * @param id Identificador UUID da categoria.
     * @return DTO de resposta do gênero encontrado.
     */
    public GeneroResponseDTO buscarPorId(String id) {
        return generoRepository.findById(id)
                .map(generoMapper::toResponseDTO)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "GÊNERO NÃO ENCONTRADO"));
    }

    /**
     * Altera o nome de um gênero existente, prevenindo colisões de nomes com outros registros.
     *
     * @param id Identificador UUID do gênero a ser editado.
     * @param dto Novos dados para o gênero.
     * @return DTO do gênero com os dados atualizados.
     */
    @Transactional
    public GeneroResponseDTO atualizar(String id, GeneroRequestDTO dto) {
        if (generoRepository.findById(id).isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "GÊNERO NÃO ENCONTRADO");
        }

        if (generoRepository.existsByNomeAndIdNot(dto.nome().trim(), id)) {
            throw new CustomException(HttpStatus.CONFLICT, "JÁ EXISTE OUTRO GÊNERO COM ESTE NOME");
        }

        Genero genero = generoMapper.toEntity(id, dto);
        generoRepository.update(genero);

        return generoMapper.toResponseDTO(genero);
    }

    /**
     * Remove um gênero literário do acervo pelo seu identificador.
     *
     * @param id Identificador UUID do gênero a ser excluído.
     */
    @Transactional
    public void excluir(String id) {
        if (generoRepository.findById(id).isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "GÊNERO NÃO ENCONTRADO");
        }

        generoRepository.deleteById(id);
    }
}
