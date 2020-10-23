package drm;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

// drm.Handler value: example.drm.Handler
public class Handler implements RequestHandler<Map<String,String>, String>{
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        String response = new String("200 OK");
        // process event
//        logger.log("EVENT: " + gson.toJson(event));
        event.entrySet().forEach(entry->{
            logger.log(entry.getKey() + " " + entry.getValue());
        });
        return response;
    }
}