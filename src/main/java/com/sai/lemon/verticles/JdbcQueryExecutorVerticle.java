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
public class JdbcQueryExecutorVerticle extends AbstractVerticle {

    private JsonObject config;
    private JsonObject recentValue;

    @Override
    public void start() throws Exception {
        this.config = config();
        vertx.eventBus().consumer(AddressPrefixType.FETCH_DATA.address(config.getString("name")), this::fetchData);

        // clear the cache.
        vertx.eventBus().consumer(AddressPrefixType.CLEAR_CACHE.address(config.getString("name")), msg -> {
            if (this.config.getString("name").equals(msg.body())) {
                this.recentValue = null;
            }
        });
    }

    private void fetchData(final Message<Object> message) {
        if (recentValue == null) {
            long timestamp = System.currentTimeMillis();
            queryFromDb();
            recentValue = new JsonObject("{\"key\": \"value\", \"timestamp\": " + timestamp + "}");
        }
        vertx.eventBus().send(AddressPrefixType.TRANSFORM_DATA.address(config.getString("name")), recentValue);
    }

    private void queryFromDb() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return this.getClass().getCanonicalName() + ":" + this.hashCode() + " [" + config.getString("name") + "]";
    }
}
