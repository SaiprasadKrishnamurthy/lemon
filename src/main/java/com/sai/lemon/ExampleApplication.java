package com.sai.lemon;

import org.apache.commons.dbcp2.BasicDataSource;
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

    public static void main(String[] args) throws Exception {
        System.setProperty("lemon.configs.dir", "/Users/saipkri/learning/lemon/configs");
        ApplicationContext context = new AnnotationConfigApplicationContext(ExampleApplication.class);
        /*ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        //System.out.println(mapper.writeValueAsString(mapper.readValue(new File("configs/lemon-config.yaml"), LemonConfig[].class)));

        LemonConfig c = new LemonConfig();
        c.setName("foo");
        c.setDataSourceSpringBeanName("ds spring");
        Visualization v1 = new Visualization();
        v1.setName("v1 name");
        v1.setSql("sql name");

        c.setVisualizations(new Visualization[]{v1, v1});

        System.out.println(mapper.writeValueAsString(c));

        // Scale the verticles on cores: create 4 instances during the deployment
*/

    }
}