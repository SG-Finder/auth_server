package com.finder.genie_ai.model.game.player.response_config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.finder.genie_ai.model.game.player.PlayerModel;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class PlayerModelSerializer extends JsonSerializer<PlayerModel> {

    @Override
    public void serialize(PlayerModel playerModel,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("nickname", playerModel.getNickname());
        jsonGenerator.writeStringField("tier", playerModel.getTier().toString());
        jsonGenerator.writeStringField("score", Integer.toString(playerModel.getScore()));
        jsonGenerator.writeStringField("rank", Integer.toString(playerModel.getRank()));
        jsonGenerator.writeStringField("point", Integer.toString(playerModel.getPoint()));
        jsonGenerator.writeEndObject();
    }

}
