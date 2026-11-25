package com.br.The_Reading_Nest_Back.services;

import com.br.The_Reading_Nest_Back.entities.Aluguel;
import com.br.The_Reading_Nest_Back.entities.PerfilUsuario;
import com.br.The_Reading_Nest_Back.entities.StatusAluguel;
import com.br.The_Reading_Nest_Back.entities.StatusConta;
import com.br.The_Reading_Nest_Back.entities.Usuario;
import com.br.The_Reading_Nest_Back.exceptions.CustomException;
import com.br.The_Reading_Nest_Back.mappers.AluguelMapper;
import com.br.The_Reading_Nest_Back.repositories.AluguelRepository;
import com.br.The_Reading_Nest_Back.repositories.LivroRepository;
import com.br.The_Reading_Nest_Back.repositories.UsuarioRepository;
import com.br.The_Reading_Nest_Back.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AluguelServiceTest {

    private final AluguelRepository aluguelRepository = mock(AluguelRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final AluguelService aluguelService = new AluguelService(
            aluguelRepository,
            mock(LivroRepository.class),
            usuarioRepository,
            mock(AluguelMapper.class),
            new SecurityUtils()
    );

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listaHistoricoDoUsuarioAutenticadoIncluindoAlugueisConcluidos() {
        Usuario usuario = usuario("cliente-123", PerfilUsuario.cliente);
        autenticar(usuario);
        PageRequest pageable = PageRequest.of(0, 30);
        Aluguel concluido = new Aluguel(
                "aluguel-123",
                usuario.id(),
                usuario.nome(),
                LocalDateTime.of(2026, 11, 20, 10, 0),
                LocalDate.of(2026, 12, 4),
                StatusAluguel.concluido
        );
        Page<Aluguel> historico = new PageImpl<>(List.of(concluido), pageable, 1);
        when(usuarioRepository.findById(usuario.id())).thenReturn(Optional.of(usuario));
        when(aluguelRepository.findByUsuarioId(usuario.id(), pageable)).thenReturn(historico);

        Page<Aluguel> resultado = aluguelService.listarMeus(pageable);

        assertEquals(historico, resultado);
        assertEquals(StatusAluguel.concluido, resultado.getContent().getFirst().status());
        verify(aluguelRepository).findByUsuarioId(usuario.id(), pageable);
    }

    @Test
    void impedeClienteDeConsultarHistoricoDeOutroUsuario() {
        Usuario autenticado = usuario("cliente-123", PerfilUsuario.cliente);
        autenticar(autenticado);

        CustomException erro = assertThrows(
                CustomException.class,
                () -> aluguelService.listarPorUsuario("outro-cliente-456", PageRequest.of(0, 30))
        );

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatus());
        verifyNoInteractions(usuarioRepository, aluguelRepository);
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                )
        );
    }

    private Usuario usuario(String id, PerfilUsuario perfil) {
        return new Usuario(id, "Leitor de teste", "leitor@example.com", "hash-interno", perfil, StatusConta.ativo);
    }
}
