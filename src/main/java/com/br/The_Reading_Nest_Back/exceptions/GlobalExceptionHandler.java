package com.br.The_Reading_Nest_Back.exceptions;

import com.br.The_Reading_Nest_Back.dtos.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Explicação simples:
 * O guardião central de erros do backend. Quando qualquer parte da aplicação falha (ex: alguém
 * digita dados inválidos, tenta invadir uma área sem permissão, uma rota não existe ou o banco recusa uma exclusão),
 * esta classe intercepta o problema e devolve sempre uma resposta padronizada, bonita e fácil de entender para o frontend.
 *
 * Explicação técnica:
 * Componente anotado com @RestControllerAdvice que intercepta globalmente exceções lançadas nos Controllers e camadas inferiores.
 * Converte cada tipo específico de exceção na estrutura uniforme 'ErrorResponseDTO' com o status HTTP adequado,
 * carimbo de data/hora (LocalDateTime.now()) e lista de erros legíveis, tratando nativamente falhas de segurança
 * do Spring Security (AccessDeniedException e AuthenticationException) e registrando falhas não tratadas
 * no log do servidor através da interface SLF4J (log.error).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Intercepta erros de validação de DTOs disparados pelo Jakarta Bean Validation (@Valid).
     *
     * @param ex Exceção capturada com a lista de violações de campos.
     * @return Resposta com status 400 Bad Request e a lista de mensagens de erro em maiúsculas.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> erros = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erros.add(fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage().toUpperCase() : "CAMPO INVÁLIDO");
        }
        if (erros.isEmpty()) {
            erros.add("DADOS DA REQUISIÇÃO INVÁLIDOS");
        }

        ErrorResponseDTO resposta = new ErrorResponseDTO(HttpStatus.BAD_REQUEST.value(), erros, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    /**
     * Intercepta requisições com JSON malformado, sintaxe quebrada ou tipos de data ilegíveis.
     *
     * @param ex Exceção disparada quando o Jackson não consegue desserializar o corpo HTTP.
     * @return Resposta com status 400 Bad Request e mensagem clara de formato inválido.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotReadableException(HttpMessageNotReadableException ex) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                List.of("CORPO DA REQUISIÇÃO INVÁLIDO OU FORMATO DE DATA INCORRETO"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    /**
     * Intercepta conversões inválidas de parâmetros de rota ou URL (ex: passar texto em campo numérico).
     *
     * @param ex Exceção gerada na conversão de parâmetros da URL.
     * @return Resposta com status 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                List.of("PARÂMETRO DA REQUISIÇÃO INVÁLIDO OU TIPO INCOMPATÍVEL"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    /**
     * Intercepta rotas e endpoints inexistentes no servidor.
     *
     * @param ex Exceção do Spring quando nenhum endpoint ou recurso estático corresponde ao caminho.
     * @return Resposta com status 404 Not Found.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResourceFound(NoResourceFoundException ex) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                List.of("ROTA NÃO ENCONTRADA"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    /**
     * Intercepta métodos HTTP não suportados para a rota (ex: enviar POST em rota que aceita apenas GET).
     *
     * @param ex Exceção disparada pelo framework web.
     * @return Resposta com status 405 Method Not Allowed.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                List.of("MÉTODO HTTP NÃO SUPORTADO PARA ESTA ROTA"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(resposta);
    }

    /**
     * Intercepta falhas de autorização do Spring Security disparadas em nível de método ou controller.
     *
     * @param ex Exceção de acesso negado por falta de privilégio hierárquico.
     * @return Resposta com status 403 Forbidden padronizada em JSON.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                HttpStatus.FORBIDDEN.value(),
                List.of("ACESSO NEGADO: VOCÊ NÃO TEM PERMISSÃO PARA ACESSAR ESTE RECURSO"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resposta);
    }

    /**
     * Intercepta falhas de autenticação do Spring Security disparadas em tempo de execução.
     *
     * @param ex Exceção de falha de autenticação ou credencial inválida.
     * @return Resposta com status 401 Unauthorized padronizada em JSON.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                HttpStatus.UNAUTHORIZED.value(),
                List.of("ACESSO NÃO AUTORIZADO: CREDENCIAIS INVÁLIDAS OU NÃO FORNECIDAS"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resposta);
    }

    /**
     * Intercepta as regras de negócio disparadas intencionalmente pela aplicação.
     *
     * @param ex Exceção de negócio com status HTTP e lista de erros definidos no Service.
     * @return Resposta contendo o status da exceção e a lista de motivos da recusa.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomException(CustomException ex) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                ex.getStatus().value(),
                ex.getErros(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(ex.getStatus()).body(resposta);
    }

    /**
     * Intercepta falhas de integridade referencial no banco de dados (ex: tentar apagar gênero em uso).
     *
     * @param ex Exceção disparada pelo driver JDBC/MySQL ao violar constraints de chave estrangeira.
     * @return Resposta com status 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                List.of("OPERAÇÃO BLOQUEADA POR INTEGRIDADE DE DADOS NO BANCO (REGISTRO EM USO)"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
    }

    /**
     * Intercepta qualquer falha inesperada no servidor que não foi tratada pelas regras anteriores.
     *
     * @param ex Exceção genérica não esperada.
     * @return Resposta com status 500 Internal Server Error, registrando a stack trace nos logs.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("Erro interno inesperado no servidor", ex);
        ErrorResponseDTO resposta = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                List.of("ERRO INTERNO NO SERVIDOR"),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
    }
}
