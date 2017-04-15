package com.sai.lemon.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by saipkri on 14/04/17.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class ClearCacheVerticle extends AbstractVerticle {

    private JsonObject config;

    @Override
    public void start() throws Exception {
        this.config = config();

    }

    public String toString() {
        return this.getClass().getCanonicalName() + ":" + this.hashCode() + " [" + config.getString("id") + "]";
    }
}
