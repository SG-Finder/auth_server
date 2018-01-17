package com.finder.genie_ai.model.token.json_config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.finder.genie_ai.model.token.SessionTokenModel;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@JsonComponent
public class SessionTokenSerializer extends JsonSerializer<SessionTokenModel> {

    @Override
    public void serialize(SessionTokenModel sessionTokenModel,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("userId", sessionTokenModel.getUserId());
                jsonGenerator.writeStringField("ip", sessionTokenModel.getIp());
                jsonGenerator.writeStringField("signinAt", sessionTokenModel.getSigninAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                jsonGenerator.writeEndObject();
            }

}
