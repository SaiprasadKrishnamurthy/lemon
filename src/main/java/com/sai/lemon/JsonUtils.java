package com.sai.lemon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;

/**
 * Created by saipkri on 16/04/17.
 */
public final class JsonUtils {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private JsonUtils() {
    }

    public static JsonObject toJsonObject(final Object in) {
        try {
            return new JsonObject(jsonMapper.writeValueAsString(in));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
