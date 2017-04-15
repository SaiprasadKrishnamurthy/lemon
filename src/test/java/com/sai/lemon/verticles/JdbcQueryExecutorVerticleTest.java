package com.sai.lemon.verticles;

import com.sai.lemon.model.AddressPrefixType;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by saipkri on 15/04/17.
 */
@RunWith(VertxUnitRunner.class)
public class JdbcQueryExecutorVerticleTest {

    private Vertx vertx = Vertx.vertx();

    @Test
    public void testSomething(final TestContext context) {
        JsonObject config = new JsonObject("{\"name\": \"someName\"}");
        vertx.deployVerticle(JdbcQueryExecutorVerticle.class.getName(), new DeploymentOptions().setWorker(true).setConfig(config));
        vertx.eventBus().send(AddressPrefixType.FETCH_DATA.address("someName"), config);
        vertx.eventBus().consumer(AddressPrefixType.TRANSFORM_DATA.address("someName"), msg -> {
            System.out.println("YIPEE --- "+msg);
        });
        vertx.close();
    }

}