package com.br.The_Reading_Nest_Back.security;

import com.br.The_Reading_Nest_Back.entities.PerfilUsuario;
import com.br.The_Reading_Nest_Back.entities.Usuario;
import com.br.The_Reading_Nest_Back.exceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Explicação simples:
 * Uma ferramenta de ajuda para saber quem é a pessoa que está usando o sistema naquele instante.
 * Permite que as regras de negócio descubram se o usuário logado é um cliente ou administrador,
 * e qual é o seu identificador para garantir que ele só mexa nas coisas dele.
 *
 * Explicação técnica:
 * Componente utilitário que inspeciona o 'SecurityContextHolder' do Spring Security para extrair
 * com tipagem segura a entidade 'Usuario' armazenada como principal no token de autenticação.
 * Provê métodos de consulta de permissões e resolução obrigatória com disparo de CustomException (HTTP 401/403).
 */
@Component
public class SecurityUtils {

    /**
     * Retorna o usuário autenticado atual, encapsulado em um Optional.
     *
     * @return Optional contendo a entidade Usuario se autenticado; Optional.empty() caso anônimo.
     */
    public Optional<Usuario> getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Usuario usuario) {
            return Optional.of(usuario);
        }

        return Optional.empty();
    }

    /**
     * Retorna o usuário autenticado atual ou lança exceção 401 caso não haja sessão válida.
     *
     * @return Entidade Usuario autenticada.
     */
    public Usuario getUsuarioAutenticadoOuFalhar() {
        return getUsuarioAutenticado().orElseThrow(() ->
                new CustomException(HttpStatus.UNAUTHORIZED, "ACESSO NÃO AUTORIZADO: USUÁRIO NÃO AUTENTICADO"));
    }

    /**
     * Verifica se o usuário autenticado possui o papel de administrador.
     *
     * @return Verdadeiro se for administrador; falso caso contrário.
     */
    public boolean isAdmin() {
        return getUsuarioAutenticado()
                .map(usuario -> usuario.perfil() == PerfilUsuario.admin)
                .orElse(false);
    }
}
