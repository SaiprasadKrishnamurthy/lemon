package com.sai.lemon.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sai.lemon.JsonUtils;
import com.sai.lemon.model.AddressPrefixType;
import com.sai.lemon.model.LemonConfig;
import com.sai.lemon.model.Visualization;
import com.sai.lemon.springbridge.SpringVerticleFactory;
import com.sai.lemon.verticles.*;
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
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 14/04/17.
 */
@Component
@Configuration
public class Bootstrap {

    @Autowired
    private final ApplicationContext applicationContext;
    private static final String lemon_data_refresh_notif_queue = "lemon_data_refresh_notif";

    public Bootstrap(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public Queue queue() {
        return new Queue(lemon_data_refresh_notif_queue, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("lemon_exchange");
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(lemon_data_refresh_notif_queue);
    }

    @PostConstruct
    public void onStartup() throws Exception {
        Vertx vertx = Vertx.vertx();
        VerticleFactory verticleFactory = applicationContext.getBean(SpringVerticleFactory.class);
        vertx.registerVerticleFactory(verticleFactory);

        final RabbitTemplate sender = new RabbitTemplate((ConnectionFactory) applicationContext.getBean("rabbitConnectionFactory"));
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
            sender.convertAndSend(lemon_data_refresh_notif_queue, ctx.request().getParam("name"));
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

        String configsDir = System.getProperty("lemon.configs.dir");
        if (org.apache.commons.lang3.StringUtils.isBlank(configsDir)) {
            throw new IllegalArgumentException("System property 'lemon.configs.dir' must be specified. This directory should contain all the config yaml files.");
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ObjectMapper jsonMapper = new ObjectMapper();

        List<LemonConfig> configs = Files.list(Paths.get(configsDir))
                .flatMap(p -> {
                    try {
                        return Stream.of(config(mapper, p));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(toList());

        // needs to be done in a loop per config.
        for (LemonConfig config : configs) {
            for (Visualization vis : config.getVisualizations()) {
                JsonObject conf = new JsonObject(jsonMapper.writeValueAsString(vis));
                conf.put("jdbcTemplateName", config.getJdbcTemplateName());
                int instances = Integer.parseInt(vis.getScaleInstances().trim());
                vertx.deployVerticle(verticleFactory.prefix() + ":" + JdbcQueryExecutorVerticle.class.getName(), new DeploymentOptions().setInstances(instances).setConfig(conf).setWorker(true));
                vertx.deployVerticle(verticleFactory.prefix() + ":" + VisualizerConversionVerticle.class.getName(), new DeploymentOptions().setInstances(instances).setConfig(conf).setWorker(false));
                vertx.deployVerticle(verticleFactory.prefix() + ":" + DataTransformerVerticle.class.getName(), new DeploymentOptions().setInstances(instances).setConfig(conf).setWorker(false));
                vertx.deployVerticle(verticleFactory.prefix() + ":" + OutputDispatcherVerticle.class.getName(), new DeploymentOptions().setInstances(instances).setConfig(conf).setWorker(false));
                vertx.deployVerticle(verticleFactory.prefix() + ":" + ClearCacheVerticle.class.getName(), new DeploymentOptions().setInstances(instances).setConfig(conf).setWorker(false));
                vertx.setPeriodic(Long.parseLong(vis.getStaleDataNoMoreThanSeconds().trim()) * 1000, timerId -> vertx.eventBus().send(AddressPrefixType.FETCH_DATA.address(vis.getId()), conf));
            }
        }

        // Request data one off.
        router.get("/data").handler(ctx -> {
            for (LemonConfig config : configs) {
                for (Visualization vis : config.getVisualizations()) {
                    JsonObject conf = new JsonObject(JsonUtils.toJsonString(vis));
                    conf.put("jdbcTemplateName", config.getJdbcTemplateName());
                    vertx.eventBus().send(AddressPrefixType.FETCH_DATA_ONCE.address(vis.getId()), conf);
                }
                ctx.response().setStatusCode(201).end();
            }
        });
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(applicationContext.getBean(ConnectionFactory.class));
        container.setQueueNames(lemon_data_refresh_notif_queue);
        container.setMessageListener((MessageListener) message -> {
            vertx.eventBus().publish(AddressPrefixType.CLEAR_CACHE.address(new String(message.getBody()).trim()), new String(message.getBody()).trim());
        });
        container.initialize();
        container.start();
    }

    private LemonConfig[] config(ObjectMapper mapper, Path p) throws java.io.IOException {
        return mapper.readValue(p.toFile(), LemonConfig[].class);
    }
}
