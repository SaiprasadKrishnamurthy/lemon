package com.sai.lemon.verticles;

import com.sai.lemon.JsonUtils;
import com.sai.lemon.model.AddressPrefixType;
import com.sai.lemon.model.QueryResults;
import com.sai.lemon.model.SqlProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by saipkri on 14/04/17.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class JdbcQueryExecutorVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcQueryExecutorVerticle.class);

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
        this.config = this.config == null ? config() : this.config;
        vertx.eventBus().consumer(AddressPrefixType.FETCH_DATA.address(config.getString("id")), this::fetchData);

        vertx.eventBus().consumer(AddressPrefixType.FETCH_DATA_ONCE.address(config.getString("id")), this::fetchDataOnce);

        // clear the cache.
        vertx.eventBus().consumer(AddressPrefixType.CLEAR_CACHE.address(config.getString("id")), msg -> {
            if (this.config.getString("id").equals(msg.body())) {
                this.recentValue = null;
            }
        });
    }

    void fetchDataOnce(final Message<JsonObject> objectMessage) {
        if (recentValue == null) {
            fetchData(objectMessage);
        } else {
            vertx.eventBus().send(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), recentValue);
        }
    }

    void fetchData(final Message<JsonObject> message) {
        if (jdbcTemplate == null) {
            jdbcTemplate = (JdbcTemplate) applicationContext.getBean(message.body().getString("jdbcTemplateName"));
        }
        if (message.body().getBoolean("dbPushEnabled")) {
            if (recentValue == null) {
                QueryResults rows = queryFromDb(message.body());
                recentValue = JsonUtils.toJsonObject(rows);
                vertx.eventBus().send(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), recentValue);
            }
        } else {
            QueryResults rows = queryFromDb(message.body());
            if (recentValue == null) {
                recentValue = JsonUtils.toJsonObject(rows);
                vertx.eventBus().send(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), recentValue);
            } else {
                JsonObject old = recentValue;
                recentValue = JsonUtils.toJsonObject(rows);
                if (!recentValue.toString().equals(old.toString())) {
                    vertx.eventBus().send(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), recentValue);
                }
            }
        }
    }

    private QueryResults queryFromDb(final JsonObject message) {
        LOGGER.info("Querying the database for: {} " + message.getString("id"));
        String sql = message.getString("sql");
        if (StringUtils.hasText(config.getString("sqlProviderClass"))) {
            try {
                SqlProvider sqlProvider = (SqlProvider) Class.forName(config.getString("sqlProviderClass")).newInstance();
                sql = sqlProvider.sql(config, applicationContext);
            } catch (Exception ex) {
                LOGGER.error("Error while trying to invoke the SQL Provider: {}", ex);
                throw new RuntimeException(ex);
            }
        }
        return new QueryResults(jdbcTemplate.queryForList(sql));
    }

    // For Unit testing
    void setVertx(final Vertx vertx) {
        this.vertx = vertx;
    }

    void setConfig(final JsonObject config) {
        this.config = config;
    }

    JsonObject getRecentValue() {
        return recentValue;
    }
}
