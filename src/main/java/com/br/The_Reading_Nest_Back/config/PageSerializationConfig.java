package com.br.The_Reading_Nest_Back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

@Configuration
public class PageSerializationConfig {

    @Bean
    JacksonModule pageSerializationModule() {
        return new SimpleModule("PageSerializationModule")
                .addSerializer(Page.class, new PageSerializer());
    }

    private static final class PageSerializer extends StdSerializer<Page> {

        private PageSerializer() {
            super(Page.class);
        }

        @Override
        public void serialize(Page page, JsonGenerator generator, SerializationContext context)
                throws JacksonException {
            generator.writeStartObject(page);
            generator.writeName("content");
            context.writeValue(generator, page.getContent());
            escreverNumero(generator, "totalElements", page.getTotalElements());
            escreverNumero(generator, "totalPages", page.getTotalPages());
            escreverNumero(generator, "size", page.getSize());
            escreverNumero(generator, "number", page.getNumber());
            escreverNumero(generator, "numberOfElements", page.getNumberOfElements());
            generator.writeEndObject();
        }

        private void escreverNumero(JsonGenerator generator, String nome, long valor) throws JacksonException {
            generator.writeName(nome);
            generator.writeNumber(valor);
        }
    }
}
