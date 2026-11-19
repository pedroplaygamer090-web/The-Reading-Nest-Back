package com.br.The_Reading_Nest_Back.services;

import com.br.The_Reading_Nest_Back.dtos.LivroRequestDTO;
import com.br.The_Reading_Nest_Back.entities.Genero;
import com.br.The_Reading_Nest_Back.entities.Livro;
import com.br.The_Reading_Nest_Back.entities.StatusLivro;
import com.br.The_Reading_Nest_Back.exceptions.CustomException;
import com.br.The_Reading_Nest_Back.mappers.LivroMapper;
import com.br.The_Reading_Nest_Back.repositories.GeneroRepository;
import com.br.The_Reading_Nest_Back.repositories.LivroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Explicação simples:
 * O cérebro do acervo de livros da biblioteca. Ele valida se a categoria informada existe de verdade,
 * impede que dois livros tenham o mesmo código ISBN, gera o código único do livro (UUID) e realiza
 * uma desativação segura (soft-delete), devolvendo sempre entidades Livro puras.
 *
 * Explicação técnica:
 * Camada de serviço de negócios para a entidade Livro. Orquestra regras de integridade de domínio,
 * tais como existência prévia da chave estrangeira de gênero antes da inserção, unicidade de ISBN,
 * controle transacional (@Transactional) para isolamento ACID e implementação do padrão de soft-delete,
 * devolvendo sempre a entidade pura Livro sem expor DTOs na camada de negócio.
 */
@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final GeneroRepository generoRepository;
    private final LivroMapper livroMapper;

    public LivroService(LivroRepository livroRepository, GeneroRepository generoRepository, LivroMapper livroMapper) {
        this.livroRepository = livroRepository;
        this.generoRepository = generoRepository;
        this.livroMapper = livroMapper;
    }

    /**
     * Cadastra um novo livro na biblioteca após validar integridade do gênero e unicidade do ISBN.
     *
     * @param dto Informações do livro a ser adicionado ao acervo.
     * @return Entidade Livro salva com seu UUID e nome de gênero preenchidos.
     */
    @Transactional
    public Livro criar(LivroRequestDTO dto) {
        Genero genero = generoRepository.findById(dto.generoId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "GÊNERO INFORMADO NÃO ENCONTRADO"));

        if (dto.isbn() != null && !dto.isbn().isBlank() && livroRepository.existsByIsbn(dto.isbn().trim())) {
            throw new CustomException(HttpStatus.CONFLICT, "JÁ EXISTE UM LIVRO CADASTRADO COM ESTE ISBN");
        }

        String id = UUID.randomUUID().toString();
        Livro livro = livroMapper.toEntity(id, dto);
        livroRepository.insert(livro);

        return new Livro(
                id,
                livro.generosId(),
                genero.nome(),
                livro.titulo(),
                livro.autor(),
                livro.descricao(),
                livro.anoPublicacao(),
                livro.isbn(),
                livro.quantidadeDisponivel(),
                livro.statusLivro()
        );
    }

    /**
     * Lista os livros do acervo com paginação e filtros opcionais de status e gênero.
     *
     * @param status Filtro opcional por status do livro ('ativo' ou 'desativado').
     * @param generoId Filtro opcional por identificador do gênero.
     * @param pageable Configurações de paginação do Spring Data.
     * @return Página de entidades Livro.
     */
    public Page<Livro> listar(StatusLivro status, String generoId, Pageable pageable) {
        return livroRepository.findAll(status, generoId, pageable);
    }

    /**
     * Busca os detalhes de um livro pelo seu identificador UUID.
     *
     * @param id Identificador do livro.
     * @return Entidade Livro localizada com nome de gênero.
     */
    public Livro buscarPorId(String id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "LIVRO NÃO ENCONTRADO"));
    }

    /**
     * Atualiza os dados de um livro existente no acervo.
     *
     * @param id Identificador UUID do livro em modificação.
     * @param dto Novos dados para o livro.
     * @return Entidade Livro com os dados atualizados.
     */
    @Transactional
    public Livro atualizar(String id, LivroRequestDTO dto) {
        if (livroRepository.findById(id).isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "LIVRO NÃO ENCONTRADO");
        }

        Genero genero = generoRepository.findById(dto.generoId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "GÊNERO INFORMADO NÃO ENCONTRADO"));

        if (dto.isbn() != null && !dto.isbn().isBlank() && livroRepository.existsByIsbnAndIdNot(dto.isbn().trim(), id)) {
            throw new CustomException(HttpStatus.CONFLICT, "JÁ EXISTE OUTRO LIVRO CADASTRADO COM ESTE ISBN");
        }

        Livro livro = livroMapper.toEntity(id, dto);
        livroRepository.update(livro);

        return new Livro(
                id,
                livro.generosId(),
                genero.nome(),
                livro.titulo(),
                livro.autor(),
                livro.descricao(),
                livro.anoPublicacao(),
                livro.isbn(),
                livro.quantidadeDisponivel(),
                livro.statusLivro()
        );
    }

    /**
     * Desativa um livro no acervo da biblioteca (soft-delete), preservando seu histórico de empréstimos.
     *
     * @param id Identificador UUID do livro a ser desativado.
     */
    @Transactional
    public void excluir(String id) {
        if (livroRepository.findById(id).isEmpty()) {
            throw new CustomException(HttpStatus.NOT_FOUND, "LIVRO NÃO ENCONTRADO");
        }

        if (livroRepository.hasAlugueisAtivos(id)) {
            throw new CustomException(HttpStatus.CONFLICT, "NÃO É POSSÍVEL DESATIVAR UM LIVRO COM ALUGUÉIS ATIVOS OU ATRASADOS");
        }

        livroRepository.softDelete(id);
    }
}
