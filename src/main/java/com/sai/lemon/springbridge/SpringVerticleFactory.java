package com.sai.lemon.springbridge;

import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class SpringVerticleFactory implements VerticleFactory {

    @Autowired
    private final ApplicationContext applicationContext;

    public SpringVerticleFactory(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean blockingCreate() {
        // Usually verticle instantiation is fast but since our verticles are Spring Beans,
        // they might depend on other beans/resources which are slow to build/lookup.
        return true;
    }

    @Override
    public String prefix() {
        // Just an arbitrary string which must uniquely identify the verticle factory
        return "myapp";
    }

    @Override
    public Verticle createVerticle(final String verticleName, final ClassLoader classLoader) throws Exception {
        // Our convention in this example is to give the class name as verticle name
        String clazz = VerticleFactory.removePrefix(verticleName);
        return (Verticle) applicationContext.getBean(Class.forName(clazz));
    }
}