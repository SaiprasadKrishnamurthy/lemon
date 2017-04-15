package com.sai.lemon.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.lemon.JsonUtils;
import com.sai.lemon.model.AddressPrefixType;
import com.sai.lemon.model.PieChart;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by saipkri on 14/04/17.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class VisualizerConversionVerticle extends AbstractVerticle {

    private JsonObject config;
    private final ObjectMapper jsonMapper = new ObjectMapper();


    @Override
    public void start() throws Exception {
        // Look up the config.
        this.config = config();
        vertx.eventBus().consumer(AddressPrefixType.VISUALIZER_CONVERSION.address(config.getString("id")), this::convert);
    }

    private void convert(final Message<JsonObject> message) {
        if (config.getString("type").equalsIgnoreCase("pie")) {
            PieChart pie = new PieChart();
            pie.setId(message.body().getString("id"));
            List<Map<String, Object>> resultsArr = message.body().getJsonArray("results").getList();
            for (Map<String, Object> result : resultsArr) {
                pie.addAllData(result);
            }
            message.reply(JsonUtils.toJsonObject(pie));
        } else if (config.getString("type").equalsIgnoreCase("line")) {
            // TODO
        } else if (config.getString("type").equalsIgnoreCase("json")) {
            message.reply(message);
        } else if (config.getString("type").equalsIgnoreCase("bar")) {
            // TODO
        }
    }
}
