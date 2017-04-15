package com.sai.lemon.model;

import io.vertx.core.json.JsonObject;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 14/04/17.
 */
public interface DataTransformer {
    JsonObject transformationFunction(ApplicationContext applicationContext, JsonObject jsonIn);
}
