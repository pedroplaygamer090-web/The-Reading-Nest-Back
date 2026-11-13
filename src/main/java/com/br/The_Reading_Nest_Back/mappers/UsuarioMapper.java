package com.br.The_Reading_Nest_Back.mappers;

import com.br.The_Reading_Nest_Back.dtos.UsuarioRequestDTO;
import com.br.The_Reading_Nest_Back.dtos.UsuarioResponseDTO;
import com.br.The_Reading_Nest_Back.entities.PerfilUsuario;
import com.br.The_Reading_Nest_Back.entities.StatusConta;
import com.br.The_Reading_Nest_Back.entities.Usuario;
import org.springframework.stereotype.Component;

/**
 * Explicação simples:
 * Traduz as informações de cadastro de usuários (como nome, e-mail, senha e perfil) para o formato interno
 * do sistema e, na hora de devolver os dados para a tela, remove a senha por completo para proteger o usuário.
 *
 * Explicação técnica:
 * Componente @Component que materializa a entidade imutável Usuario a partir do DTO de requisição
 * associado ao hash seguro gerado pelo BCryptPasswordEncoder e ao perfil validado pelas regras de negócio,
 * além de projetar a entidade no 'UsuarioResponseDTO' higienizado e totalmente desprovido de qualquer atributo confidencial.
 */
@Component
public class UsuarioMapper {

    /**
     * Converte os dados de cadastro em uma entidade Usuario com senha criptografada, perfil e status ativo.
     *
     * @param id Identificador UUID gerado pelo serviço.
     * @param dto Dados de entrada validados com nome e e-mail.
     * @param senhaCriptografada Hash seguro da senha produzido pelo PasswordEncoder.
     * @param perfil Perfil de acesso determinado pelo serviço (admin ou cliente).
     * @return Entidade Usuario pronta para persistência.
     */
    public Usuario toEntity(String id, UsuarioRequestDTO dto, String senhaCriptografada, PerfilUsuario perfil) {
        PerfilUsuario perfilFinal = perfil != null ? perfil : (dto.perfil() != null ? dto.perfil() : PerfilUsuario.cliente);
        return new Usuario(
                id,
                dto.nome().trim(),
                dto.email().trim().toLowerCase(),
                senhaCriptografada,
                perfilFinal,
                StatusConta.ativo
        );
    }

    /**
     * Converte a entidade de domínio Usuario no DTO de resposta pública.
     *
     * @param entity Entidade Usuario obtida na camada de repositório.
     * @return DTO de saída contendo apenas id, nome, e-mail, perfil e status da conta.
     */
    public UsuarioResponseDTO toResponseDTO(Usuario entity) {
        return new UsuarioResponseDTO(
                entity.id(),
                entity.nome(),
                entity.email(),
                entity.perfil(),
                entity.statusConta()
        );
    }
}
