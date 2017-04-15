package com.sai.lemon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sai.lemon.model.LemonConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ComponentScan("com.sai.lemon")
public class ExampleApplication {

    public static void main(String[] args) throws Exception{
//        ApplicationContext context = new AnnotationConfigApplicationContext(ExampleApplication.class);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.readValue(new File("configs/lemon-config.yaml"), LemonConfig[].class);


        // Scale the verticles on cores: create 4 instances during the deployment


    }
}