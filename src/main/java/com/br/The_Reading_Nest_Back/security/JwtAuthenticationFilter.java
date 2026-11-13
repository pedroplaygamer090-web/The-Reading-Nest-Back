package com.br.The_Reading_Nest_Back.security;

import com.br.The_Reading_Nest_Back.entities.PerfilUsuario;
import com.br.The_Reading_Nest_Back.entities.StatusConta;
import com.br.The_Reading_Nest_Back.entities.Usuario;
import com.br.The_Reading_Nest_Back.repositories.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Explicação simples:
 * O porteiro de segurança da biblioteca. Para cada pedido que chega ao servidor, ele olha se a requisição
 * trouxe o crachá digital (token JWT) no cabeçalho. Se o token for válido e o usuário estiver com a conta ativa,
 * o porteiro anota na memória quem é a pessoa e quais são seus privilégios (admin ou cliente) antes de deixar o pedido passar.
 *
 * Explicação técnica:
 * Filtro interceptador que estende OncePerRequestFilter, posicionado antes de UsernamePasswordAuthenticationFilter.
 * Inspeciona o cabeçalho HTTP 'Authorization', extrai o token com prefixo 'Bearer ', delega a validação
 * criptográfica ao JwtService, recupera a entidade Usuario correspondente no UsuarioRepository, confirma a situação
 * ativa da conta (StatusConta.ativo) e popula o SecurityContextHolder com UsernamePasswordAuthenticationToken contendo
 * a autoridade 'ROLE_ADMIN' ou 'ROLE_CLIENTE'.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isTokenValido(token)) {
                String email = jwtService.extrairEmail(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailComSenha(email);

                    if (usuarioOpt.isPresent()) {
                        Usuario usuario = usuarioOpt.get();

                        if (usuario.statusConta() == StatusConta.ativo) {
                            String roleName = usuario.perfil() == PerfilUsuario.admin ? "ROLE_ADMIN" : "ROLE_CLIENTE";
                            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));

                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    usuario,
                                    null,
                                    authorities
                            );
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
