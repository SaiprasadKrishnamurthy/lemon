package com.sai.lemon.verticles;

import com.sai.lemon.JsonUtils;
import com.sai.lemon.model.AddressPrefixType;
import com.sai.lemon.model.QueryResults;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by saipkri on 14/04/17.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class JdbcQueryExecutorVerticle extends AbstractVerticle {

    private JsonObject config;
    private JsonObject recentValue;
    private final ApplicationContext applicationContext;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcQueryExecutorVerticle(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void start() throws Exception {
        this.config = config();
        vertx.eventBus().consumer(AddressPrefixType.FETCH_DATA.address(config.getString("id")), this::fetchData);

        // clear the cache.
        vertx.eventBus().consumer(AddressPrefixType.CLEAR_CACHE.address(config.getString("id")), msg -> {
            if (this.config.getString("name").equals(msg.body())) {
                this.recentValue = null;
            }
        });
    }

    private void fetchData(final Message<JsonObject> message) {
        if (jdbcTemplate == null) {
            jdbcTemplate = (JdbcTemplate) applicationContext.getBean(message.body().getString("jdbcTemplateName"));
        }
        if (message.body().getBoolean("disableDatabasePolling")) {
            if (recentValue == null) {
                QueryResults rows = queryFromDb(message.body());
                recentValue = JsonUtils.toJsonObject(rows);
            }
        } else {
            QueryResults rows = queryFromDb(message.body());
            recentValue = JsonUtils.toJsonObject(rows);
        }
        vertx.eventBus().send(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), recentValue);
    }

    private QueryResults queryFromDb(final JsonObject message) {
        return new QueryResults(jdbcTemplate.queryForList(message.getString("sql")));
    }

    public String toString() {
        return this.getClass().getCanonicalName() + ":" + this.hashCode() + " [" + config.getString("name") + "]";
    }

    // For Unit testing
    void setVertx(final Vertx vertx) {
        this.vertx = vertx;
    }
}
