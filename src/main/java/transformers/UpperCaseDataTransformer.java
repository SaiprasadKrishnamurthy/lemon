package transformers;

import com.sai.lemon.model.DataTransformer;
import io.vertx.core.json.JsonObject;
import org.springframework.context.ApplicationContext;

/**
 * Created by saipkri on 14/04/17.
 */
public class UpperCaseDataTransformer implements DataTransformer {
    @Override
    public JsonObject transformationFunction(ApplicationContext applicationContext, JsonObject jsonIn) {
        return new JsonObject(jsonIn.toString().toUpperCase());
    }
}
