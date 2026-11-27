package com.br.The_Reading_Nest_Back.security;

import com.br.The_Reading_Nest_Back.entities.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Explicação simples:
 * Fábrica e verificadora de 'crachás digitais' (tokens JWT). Quando um usuário faz login com sucesso,
 * esta classe gera um passe inviolável e temporário com suas informações básicas (e-mail, nome e perfil).
 * Nas próximas vezes que o usuário fizer um pedido à biblioteca, esta classe confere se o crachá é legítimo e ainda está no prazo.
 *
 * Explicação técnica:
 * Componente @Service que encapsula as operações criptográficas do JSON Web Token (JJWT 0.12.6).
 * Utiliza o algoritmo simétrico HMAC-SHA256 com chave derivada de 'jwt.secret' para emitir tokens
 * compactados com subject (e-mail) e claims adicionais ('id', 'nome', 'perfil'), validando integridade,
 * assinatura digital e tempo de expiração ('jwt.expiration') com tratamento defensivo de falhas de parsing.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    /**
     * Obtém a chave criptográfica SecretKey a partir dos bytes da chave secreta configurada.
     *
     * @return SecretKey apropriada para HMAC-SHA.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gera um token JWT assinado contendo os dados essenciais e o perfil do usuário.
     *
     * @param usuario Entidade do usuário autenticado.
     * @return Token JWT assinado em formato compacto (Base64Url).
     */
    public String gerarToken(Usuario usuario) {
        long agora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(usuario.email())
                .claim("id", usuario.id())
                .claim("nome", usuario.nome())
                .claim("perfil", usuario.perfil().name())
                .issuedAt(new Date(agora))
                .expiration(new Date(agora + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrai o e-mail (subject) de um token JWT.
     *
     * @param token Token JWT recebido no cabeçalho.
     * @return E-mail do usuário ou null se o token for ilegível.
     */
    public String extrairEmail(String token) {
        Claims claims = extrairTodasAsClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Valida a assinatura, integridade estrutural e prazo de expiração do token sem lançar exceções.
     *
     * @param token Token JWT a ser verificado.
     * @return Verdadeiro se o token for válido e ativo; falso caso contrário.
     */
    public boolean isTokenValido(String token) {
        try {
            Claims claims = extrairTodasAsClaims(token);
            if (claims == null) {
                return false;
            }
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            log.warn("Falha na validação do token JWT: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Decodifica e valida o payload do token JWT utilizando a chave secreta da aplicação.
     *
     * @param token String JWT compactada.
     * @return Claims do token ou null se ocorrer erro de verificação.
     */
    private Claims extrairTodasAsClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Token JWT inválido ou corrompido: {}", ex.getMessage());
            return null;
        }
    }
}
