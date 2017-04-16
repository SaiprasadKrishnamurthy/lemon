package com.sai.lemon.verticles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.lemon.JsonUtils;
import com.sai.lemon.model.AddressPrefixType;
import com.sai.lemon.model.PieChart;
import com.sai.lemon.model.XYChart;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
            pie.setId(config.getString("id"));
            List<JsonObject> resultsArr = message.body().getJsonArray("results").getList();
            for (JsonObject result : resultsArr) {
                List<Object> values = result.getMap().values().stream().collect(Collectors.toList());
                int size = values.size();
                if (size != 2) {
                    throw new IllegalArgumentException("Supplied SQL is not sutiable for rendering a pie chart. It must have only 2 values in the select clause and the second one must be a numeric value.");
                }
                pie.addData(values.get(0).toString(), values.get(1));
            }
            message.reply(JsonUtils.toJsonObject(pie));
        } else if (config.getString("type").equalsIgnoreCase("line")) {
            XYChart xyChart = getXyChart(message);
            message.reply(JsonUtils.toJsonObject(xyChart));
        } else if (config.getString("type").equalsIgnoreCase("json")) {
            message.reply(message);
        } else if (config.getString("type").equalsIgnoreCase("bar")) {
            XYChart xyChart = getXyChart(message);
            message.reply(JsonUtils.toJsonObject(xyChart));
        }
    }

    private XYChart getXyChart(Message<JsonObject> message) {
        XYChart xyChart = new XYChart();
        xyChart.setId(config.getString("id"));
        List<JsonObject> resultsArr = message.body().getJsonArray("results").getList();
        for (JsonObject result : resultsArr) {
            List<Object> values = result.getMap().values().stream().collect(Collectors.toList());
            int size = values.size();
            if (size != 2) {
                throw new IllegalArgumentException("Supplied SQL is not sutiable for rendering a line chart. It must have only 2 values in the select clause and the second one must be a numeric value.");
            }
            xyChart.addData(values.get(0).toString(), values.get(1));
        }
        return xyChart;
    }
}
