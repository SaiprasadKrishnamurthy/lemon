package com.sai.lemon.model;

import io.vertx.core.json.JsonObject;
import org.springframework.context.ApplicationContext;

/**
 * Created by saipkri on 15/04/17.
 */
public interface SqlProvider {
    String sql(JsonObject config, ApplicationContext applicationContext);
}
