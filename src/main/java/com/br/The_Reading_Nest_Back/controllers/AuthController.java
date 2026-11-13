package com.br.The_Reading_Nest_Back.controllers;

import com.br.The_Reading_Nest_Back.dtos.LoginRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.LoginResponseDTO;
import com.br.The_Reading_Nest_Back.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Explicação simples:
 * Ponto de entrada para login de leitores e administradores no sistema da biblioteca.
 * Recebe o e-mail e a senha do usuário e, se estiverem corretos, devolve o token de acesso (JWT).
 *
 * Explicação técnica:
 * Controlador RESTful mapeado em '/auth'. Provê o endpoint público de autenticação HTTP POST /auth/login,
 * delegando as validações de credenciais e a emissão do token JWT para o AuthService.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Realiza o login do usuário no sistema emitindo o token de acesso JWT.
     *
     * @param dto Credenciais contendo e-mail e senha validados.
     * @return HTTP 200 OK com o token Bearer e os dados do usuário autenticado.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO resposta = authService.login(dto);
        return ResponseEntity.ok(resposta);
    }
}
