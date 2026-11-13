package com.br.The_Reading_Nest_Back.services;

import com.br.The_Reading_Nest_Back.dtos.LoginRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.LoginResponseDTO;
import com.br.The_Reading_Nest_Back.entities.StatusConta;
import com.br.The_Reading_Nest_Back.entities.Usuario;
import com.br.The_Reading_Nest_Back.exceptions.CustomException;
import com.br.The_Reading_Nest_Back.mappers.UsuarioMapper;
import com.br.The_Reading_Nest_Back.repositories.UsuarioRepository;
import com.br.The_Reading_Nest_Back.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Explicação simples:
 * Responsável pelo processo de autenticação de usuários na biblioteca.
 * Confere se o e-mail existe, se a senha confere com o código seguro salvo no banco,
 * se a conta está ativa e, caso tudo esteja correto, emite o token JWT para o usuário navegar com segurança.
 *
 * Explicação técnica:
 * Camada de serviço de autenticação. Realiza busca de credenciais completas via UsuarioRepository,
 * compara a senha em texto puro contra o hash BCrypt salvo utilizando PasswordEncoder.matches(),
 * valida o ciclo de vida da conta impedindo acesso de usuários com StatusConta diferente de 'ativo' (HTTP 403),
 * e orquestra a geração de token assinado no JwtService, devolvendo o LoginResponseDTO tipado.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioMapper = usuarioMapper;
    }

    /**
     * Autentica um usuário com base no e-mail e senha, retornando o token de acesso Bearer e dados do usuário.
     *
     * @param dto Dados de entrada contendo e-mail e senha.
     * @return DTO contendo o tipo Bearer, o token JWT gerado e as informações seguras do usuário logado.
     */
    public LoginResponseDTO login(LoginRequestDTO dto) {
        // 1. Busca usuário por e-mail com a senha confidencial para verificação interna
        Usuario usuario = usuarioRepository.findByEmailComSenha(dto.email().trim())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "E-MAIL OU SENHA INVÁLIDOS"));

        // 2. Compara a senha em texto puro com o hash criptográfico BCrypt
        if (!passwordEncoder.matches(dto.senha(), usuario.senha())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "E-MAIL OU SENHA INVÁLIDOS");
        }

        // 3. Verifica a situação da conta do usuário
        if (usuario.statusConta() != StatusConta.ativo) {
            throw new CustomException(HttpStatus.FORBIDDEN, "CONTA SUSPENSA OU DESATIVADA. ENTRE EM CONTATO COM A BIBLIOTECA");
        }

        // 4. Gera o token JWT assinado digitalmente
        String token = jwtService.gerarToken(usuario);

        // 5. Retorna a resposta contendo o token e os dados públicos do usuário
        return new LoginResponseDTO("Bearer", token, usuarioMapper.toResponseDTO(usuario));
    }
}
