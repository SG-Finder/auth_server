package com.finder.genie_ai.model.session_manage.json_config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.finder.genie_ai.model.session_manage.SessionUserInfoModel;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class SessionUserInfoSerializer extends JsonSerializer<SessionUserInfoModel> {

    @Override
    public void serialize(SessionUserInfoModel userInfo,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("ip", userInfo.getUserIp());
                jsonGenerator.writeStringField("token", userInfo.getToken());
                jsonGenerator.writeEndObject();
            }

}
