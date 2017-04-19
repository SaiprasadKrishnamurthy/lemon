package com.sai.lemon;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
@ComponentScan("com.sai.lemon")
public class ExampleApplication {

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl("jdbc:postgresql://localhost:5432/lemon");
        ds.setInitialSize(6);
        return new JdbcTemplate(ds);
    }

    @Bean(name = "rabbitConnectionFactory")
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("lemon.configs.dir", "/Users/saipkri/learning/lemon/configs");
        ApplicationContext context = new AnnotationConfigApplicationContext(ExampleApplication.class);
    }
}