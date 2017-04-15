package com.sai.lemon.verticles;

import com.sai.lemon.model.AddressPrefixType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by saipkri on 14/04/17.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class OutputDispatcherVerticle extends AbstractVerticle {

    private JsonObject config;

    @Override
    public void start() throws Exception {
        // Look up the config.
        this.config = config();
        vertx.eventBus().consumer(AddressPrefixType.OUTPUT_DISPATCHER.address(config.getString("id")), this::broadcast);
    }

    private void broadcast(final Message<JsonObject> message) {
        // Convert to the appropriate visualizer model.
        vertx.eventBus().send(AddressPrefixType.VISUALIZER_CONVERSION.address(config.getString("id")), message.body(), reply -> {
            vertx.eventBus().publish(AddressPrefixType.OUTPUT.getValue(), message.body());
        });
    }
}
