package com.br.The_Reading_Nest_Back.config;

import com.br.The_Reading_Nest_Back.dtos.AluguelResumoResponseDTO;
import com.br.The_Reading_Nest_Back.entities.StatusAluguel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PageSerializationConfigTest {

    @Autowired
    private JsonMapper mapper;

    @Test
    void serializaPaginaNoFormatoEstavelDoSpringData() throws Exception {
        Page<String> pagina = new PageImpl<>(List.of("livro"), PageRequest.of(0, 100), 201);

        String json = mapper.writeValueAsString(pagina);

        assertEquals(
                "{\"content\":[\"livro\"],\"totalElements\":201,\"totalPages\":3,"
                        + "\"size\":100,\"number\":0,\"numberOfElements\":1}",
                json
        );
    }

    @Test
    void serializaPaginaComResumoDeAluguel() throws Exception {
        AluguelResumoResponseDTO aluguel = new AluguelResumoResponseDTO(
                "aluguel-id",
                "usuario-id",
                "Leitor",
                LocalDateTime.of(2026, 11, 22, 18, 0),
                LocalDate.of(2026, 11, 29),
                StatusAluguel.ativo
        );
        Page<AluguelResumoResponseDTO> pagina = new PageImpl<>(
                List.of(aluguel),
                PageRequest.of(0, 50),
                1
        );

        String json = mapper.writeValueAsString(pagina);

        assertEquals(
                "{\"content\":[{\"id\":\"aluguel-id\",\"usuarioId\":\"usuario-id\","
                        + "\"usuarioNome\":\"Leitor\",\"dataAluguel\":\"2026-11-22T18:00:00\","
                        + "\"dataDevolucaoPrevista\":\"2026-11-29\",\"status\":\"ativo\"}],"
                        + "\"totalElements\":1,\"totalPages\":1,\"size\":50,\"number\":0,"
                        + "\"numberOfElements\":1}",
                json
        );
    }
}
