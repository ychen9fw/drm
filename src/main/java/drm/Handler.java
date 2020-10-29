package drm;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.*;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

// drm.Handler value: example.drm.Handler
public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
//    @Override
//    public String handleRequest(Map<String,String> event, Context context)
//    {
//        LambdaLogger logger = context.getLogger();
//        String response = new String("200 OK");
//        // process event
////        logger.log("EVENT: " + gson.toJson(event));
//        event.entrySet().forEach(entry->{
//            logger.log(entry.getKey() + " " + entry.getValue());
//        });
//        return response;
//    }
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {

        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
        try {
            String requestString = apiGatewayProxyRequestEvent.getBody();
            System.out.println(requestString);
            String requestMessage = null;
            String responseMessage = null;
            if (requestString != null) {
                JsonParser parser = new JsonParser();
                JsonObject requestJsonObject = (JsonObject) parser.parse(requestString);
                if (requestJsonObject != null) {
                    if (requestJsonObject.get("requestMessage") != null) {
                        requestMessage = requestJsonObject.get("requestMessage").toString();
                    }
                }
            }
            Map<String, String> responseBody = new HashMap<String, String>();
            responseBody.put("responseMessage", requestMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiGatewayProxyResponseEvent;
    }
}