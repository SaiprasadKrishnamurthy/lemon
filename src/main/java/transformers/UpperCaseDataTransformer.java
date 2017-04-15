package transformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.lemon.model.DataTransformer;
import io.vertx.core.json.JsonObject;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * Created by saipkri on 14/04/17.
 */
public class UpperCaseDataTransformer implements DataTransformer {
    @Override
    public JsonObject transformationFunction(ApplicationContext applicationContext, JsonObject jsonIn) {
        return jsonIn;
    }
}
