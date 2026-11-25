package com.br.The_Reading_Nest_Back.config;

import com.br.The_Reading_Nest_Back.services.AluguelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PageableWebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AluguelService aluguelService;

    @Test
    void resolvePageableNaRotaDeHistoricoComTamanhoPadrao() throws Exception {
        doAnswer(invocacao -> paginaVazia(invocacao.getArgument(0)))
                .when(aluguelService).listarMeus(any(Pageable.class));

        mockMvc.perform(get("/alugueis/meus"))
                .andExpect(status().isOk());

        var pageable = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(aluguelService).listarMeus(pageable.capture());
        assertEquals(30, pageable.getValue().getPageSize());
        assertEquals(0, pageable.getValue().getPageNumber());
    }

    @Test
    void limitaTamanhoMaximoDasListagensPaginadas() throws Exception {
        doAnswer(invocacao -> paginaVazia(invocacao.getArgument(0)))
                .when(aluguelService).listarNaoConcluidos(any(Pageable.class));

        mockMvc.perform(get("/alugueis").param("page", "0").param("size", "500"))
                .andExpect(status().isOk());

        var pageable = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(aluguelService).listarNaoConcluidos(pageable.capture());
        assertEquals(100, pageable.getValue().getPageSize());
    }

    private Page<?> paginaVazia(Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }
}
