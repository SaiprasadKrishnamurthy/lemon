package com.sai.lemon.core;

import com.sai.lemon.model.AddressPrefixType;
import com.sai.lemon.springbridge.SpringVerticleFactory;
import com.sai.lemon.verticles.ClearCacheVerticle;
import com.sai.lemon.verticles.DataTransformerVerticle;
import com.sai.lemon.verticles.JdbcQueryExecutorVerticle;
import com.sai.lemon.verticles.OutputDispatcherVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.impl.RouterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by saipkri on 14/04/17.
 */
@Component
public class Bootstrap {

    @Autowired
    private final ApplicationContext applicationContext;


    public Bootstrap(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void onStartup() {
        Vertx vertx = Vertx.vertx();
        VerticleFactory verticleFactory = applicationContext.getBean(SpringVerticleFactory.class);
        vertx.registerVerticleFactory(verticleFactory);

        Router router = new RouterImpl(vertx);

        HttpServer httpServer = vertx.createHttpServer();

        router.get("/rsc/*").handler(ctx -> {
            String filename = ctx.request().path().substring(1);
            vertx.fileSystem().exists(filename, b -> {
                if (b.result()) {
                    ctx.response().sendFile(filename);
                } else {
                    ctx.fail(404);
                }
            });
        });

        // Cache clear
        router.get("/cache-clear").handler(ctx -> {
            vertx.eventBus().publish(AddressPrefixType.CLEAR_CACHE.address(ctx.request().getParam("name")), ctx.request().getParam("name"));
            ctx.response().setStatusCode(200).end("Cache cleared!");
        });

        router.route().handler(CorsHandler.create("*").allowedMethods(new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.PATCH, HttpMethod.POST, HttpMethod.PUT, HttpMethod.HEAD, HttpMethod.DELETE))));
        SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(2000);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
        BridgeOptions bo = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress("/output"))
                .addOutboundPermitted(new PermittedOptions().setAddress(AddressPrefixType.OUTPUT.getValue()));
        sockJSHandler.bridge(bo, event -> {
            event.complete(true);
        });

        router.route("/output" + "/*").handler(sockJSHandler);

        router.get("/").handler(ctx -> {
            ctx.reroute("rsc/example.html");
        });

        String portOverride = System.getProperty("lemon.web.port");
        httpServer.requestHandler(router::accept).listen(StringUtils.hasText(portOverride) ? Integer.parseInt(portOverride.trim()) : 8765);


        JsonObject config1 = new JsonObject("{\"name\": \"clientCount\"}");

        // needs to be done in a loop per config.
        vertx.deployVerticle(verticleFactory.prefix() + ":" + JdbcQueryExecutorVerticle.class.getName(), new DeploymentOptions().setInstances(1).setConfig(config1).setWorker(true));
        vertx.deployVerticle(verticleFactory.prefix() + ":" + DataTransformerVerticle.class.getName(), new DeploymentOptions().setInstances(1).setConfig(config1).setWorker(false));
        vertx.deployVerticle(verticleFactory.prefix() + ":" + OutputDispatcherVerticle.class.getName(), new DeploymentOptions().setInstances(1).setConfig(config1).setWorker(false));
        vertx.deployVerticle(verticleFactory.prefix() + ":" + ClearCacheVerticle.class.getName(), new DeploymentOptions().setInstances(1).setConfig(config1).setWorker(false));

        vertx.setPeriodic(1000, timerId -> vertx.eventBus().send(AddressPrefixType.FETCH_DATA.address(config1.getString("name")), config1));
    }
}
