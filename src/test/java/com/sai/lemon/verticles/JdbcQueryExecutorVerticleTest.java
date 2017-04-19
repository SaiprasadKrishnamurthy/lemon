package com.sai.lemon.verticles;

import com.sai.lemon.model.AddressPrefixType;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static com.sai.lemon.verticles.VerticlesMockUtils.withinTryCatch;
import static org.mockito.Mockito.*;

/**
 * Created by saipkri on 15/04/17.
 */
@RunWith(VertxUnitRunner.class)
public class JdbcQueryExecutorVerticleTest {

    private ApplicationContext applicationContext = mock(ApplicationContext.class);
    private JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    private JdbcQueryExecutorVerticle jdbcQueryExecutorVerticle;
    private JsonObject config;

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    private Vertx vertx;
    private final String sql = "select * from foo";


    @Before
    public void setup() {
        config = new JsonObject("{\"id\": \"DeviceTypeCount\", \"jdbcTemplateName\": \"jdbcTemplate\", \"dbPushEnabled\": true, \"sql\": \"" + sql + "\"}");
        jdbcQueryExecutorVerticle = new JdbcQueryExecutorVerticle(applicationContext);
        jdbcQueryExecutorVerticle.setConfig(config);
        vertx = rule.vertx();
        vertx.deployVerticle(jdbcQueryExecutorVerticle);
    }

    @Test(timeout = 1000L)
    public void shouldFetchDataForDBPushEnabledScenario(TestContext context) {
        Async async = context.async();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("COL1", 10);
        row.put("COL2", 10);
        List<Map<String, Object>> rowsReturnedFromDb = Arrays.asList(row);
        Map<String, Object> expectedOutputMessage = new HashMap<>();
        expectedOutputMessage.put("results", rowsReturnedFromDb);
        when(applicationContext.getBean("jdbcTemplate")).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(sql)).thenReturn(rowsReturnedFromDb);
        vertx.eventBus().send(AddressPrefixType.FETCH_DATA.address(config.getString("id")), config);
        vertx.eventBus().consumer(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), actualMessage -> {
            context.assertEquals(new JsonObject(expectedOutputMessage), actualMessage.body());
            withinTryCatch(context, () -> verify(jdbcTemplate, times(1)).queryForList(sql));
            async.complete();
        });
    }

    @Test(timeout = 1000L)
    public void shouldFetchDataForDBPushEnabledScenarioAndShouldQueryDBOnlyOnce(TestContext context) {
        Async async = context.async();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("COL1", 10);
        row.put("COL2", 10);
        List<Map<String, Object>> rowsReturnedFromDb = Arrays.asList(row);
        Map<String, Object> expectedOutputMessage = new HashMap<>();
        expectedOutputMessage.put("results", rowsReturnedFromDb);
        when(applicationContext.getBean("jdbcTemplate")).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(sql)).thenReturn(rowsReturnedFromDb);
        for (int i = 0; i < 10; i++) {
            vertx.eventBus().send(AddressPrefixType.FETCH_DATA.address(config.getString("id")), config);
        }
        vertx.eventBus().consumer(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), actualMessage -> {
            context.assertEquals(new JsonObject(expectedOutputMessage), actualMessage.body());
            withinTryCatch(context, () -> verify(jdbcTemplate, times(1)).queryForList(sql));
            async.complete();
        });
    }

    @Test(timeout = 4000L)
    public void shouldFetchDataForDBPullScenarioShouldQueryDBMoreThanOnce(TestContext context) {
        // mutate the config.
        config = new JsonObject("{\"id\": \"DeviceTypeCount\", \"jdbcTemplateName\": \"jdbcTemplate\", \"dbPushEnabled\": false, \"sql\": \"" + sql + "\"}");
        Async async = context.async();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("COL1", 10);
        row.put("COL2", 10);
        List<Map<String, Object>> rowsReturnedFromDb = Arrays.asList(row);
        Map<String, Object> expectedOutputMessage = new HashMap<>();
        expectedOutputMessage.put("results", rowsReturnedFromDb);
        when(applicationContext.getBean("jdbcTemplate")).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(sql)).thenReturn(rowsReturnedFromDb);
        for (int i = 0; i < 10; i++) {
            vertx.eventBus().send(AddressPrefixType.FETCH_DATA.address(config.getString("id")), config);
        }
        vertx.eventBus().consumer(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), actualMessage -> {
            context.assertEquals(new JsonObject(expectedOutputMessage), actualMessage.body());
            withinTryCatch(context, () -> verify(jdbcTemplate, atLeast(2)).queryForList(sql));
            async.complete();
        });
    }

    @Test(timeout = 4000L)
    public void shouldFetchDataForDBPullScenarioShouldNotifyConsumersOnlyOnceWhenNoChanges(final TestContext context) throws Exception {
        // mutate the config.
        config = new JsonObject("{\"id\": \"DeviceTypeCount\", \"jdbcTemplateName\": \"jdbcTemplate\", \"dbPushEnabled\": false, \"sql\": \"" + sql + "\"}");
        Async async = context.async(1);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("COL1", 10);
        row.put("COL2", 10);

        Map<String, Object> expectedOutputMessage = new HashMap<>();
        expectedOutputMessage.put("results", Arrays.asList(row));
        when(applicationContext.getBean("jdbcTemplate")).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(sql)).thenReturn(Arrays.asList(row));
        for (int i = 0; i < 5; i++) {
            vertx.eventBus().send(AddressPrefixType.FETCH_DATA.address(config.getString("id")), config);
        }
        vertx.eventBus().consumer(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), actualMessage -> async.countDown());
        context.assertEquals(1, async.count());
    }

    @Test(timeout = 4000L)
    public void clearCacheShouldClearRecentValueAndReNotifyConsumers(final TestContext context) throws Exception {
        Async async = context.async(2);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("COL1", 10);
        row.put("COL2", 10);

        Map<String, Object> expectedOutputMessage = new HashMap<>();
        expectedOutputMessage.put("results", Arrays.asList(row));
        when(applicationContext.getBean("jdbcTemplate")).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(sql)).thenReturn(Arrays.asList(row));


        // Trigger fetch messages.
        for (int i = 0; i < 5; i++) {
            vertx.eventBus().send(AddressPrefixType.FETCH_DATA.address(config.getString("id")), config);
        }

        // Clear cache instruction.
        vertx.eventBus().send(AddressPrefixType.CLEAR_CACHE.address(config.getString("id")), config.getString("id"));

        vertx.eventBus().consumer(AddressPrefixType.TRANSFORM_DATA.address(config.getString("id")), actualMessage -> async.countDown());

        // Send fetch messages again.
        for (int i = 0; i < 5; i++) {
            vertx.eventBus().send(AddressPrefixType.FETCH_DATA.address(config.getString("id")), config);
        }
    }
}