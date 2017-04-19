package com.sai.lemon.verticles;

import com.sai.lemon.model.AddressPrefixType;
import com.sai.lemon.model.DataTransformer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by saipkri on 14/04/17.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class DataTransformerVerticle extends AbstractVerticle {

    private JsonObject config;
    private final ApplicationContext applicationContext;

    @Autowired
    public DataTransformerVerticle(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() throws Exception {
        // Look up the config.
        this.config = config();
        vertx.eventBus().consumer(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), this::transformData);
    }

    private void transformData(final Message<JsonObject> message) {
        try {
            JsonObject transformedData = message.body();
            if (StringUtils.hasText(config.getString("dataTransfomerClass"))) {
                DataTransformer transformerClass = (DataTransformer) Class.forName(config.getString("dataTransfomerClass")).newInstance(); // TODO get from config
                transformedData = transformerClass.apply(applicationContext, message.body());
            }
            vertx.eventBus()
                    .send(AddressPrefixType.AUDIT_DATA.address(config.getString("id")), transformedData);

            vertx.eventBus()
                    .send(AddressPrefixType.OUTPUT_DISPATCHER.address(config.getString("id")), transformedData);
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException e
                )

        {
            e.printStackTrace();
        }

    }

    public String toString() {
        return this.getClass().getCanonicalName() + ":" + this.hashCode() + " [" + config.getString("id") + "]";
    }
}
