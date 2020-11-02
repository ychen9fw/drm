package drm;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.*;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;

import static drm.RequestDRM.doCipher;
import static drm.RequestDRM.hexStringToByteArray;

// drm.Handler value: example.drm.Handler
public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        final String URL = "https://drmkit.hwcloudtest.cn:8080/drmproxy/v2/getLicense";
        final String AES = "GujM0OeYXMC2IDpVhVoQNK/CUpgOlwypDlICaJ+Uerk=";
        final String XAPPID = "TestForDeveloper";
        final String SIGN_KEY = "VAv4XeXRNpmZEwJYQ878J5lNCbmxZpxwU2z57wmbYnA=";
        String id = "1";
        byte[] IV = null;
        byte[] CONTENTKEY = null;
        String keyid = "";

        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
        try {
            String requestString = apiGatewayProxyRequestEvent.getBody();
            System.out.println("body " + requestString);

            Map<String, String> reqHeaders = apiGatewayProxyRequestEvent.getQueryStringParameters();
            if (reqHeaders.get("id") != null) {
                id = reqHeaders.get("id");
            }
            switch (id) {
                case "1":
                    IV = Base64.getDecoder().decode("wuK1dCnuvE+gDRoWOkLKmQ==");
                    CONTENTKEY = hexStringToByteArray("3c2649fb74311591ca066c9888d890a2");
                    keyid = "8e3092475d0e434f94dbf108e334536d";

                    break;
                case "2":
                    IV = Base64.getDecoder().decode("wuK1dCnuvE+gDRoWOkLKmQ==");
                    CONTENTKEY = hexStringToByteArray("1234");
                    keyid = "1234";
                    break;
                default:
                    break;
            }

            String path = "/drmproxy/v2/getLicense";
            String xtimestamp = Long.toString(new Date().getTime());
            String begin = Long.toString(new Date().getTime()/1000);
            String expire = Long.toString(new Date().getTime()/1000 + 86400);
            byte[] key = doCipher(1, CONTENTKEY, Base64.getDecoder().decode(AES), IV);

            String keyString = Base64.getEncoder().encodeToString(key);
            String IVString = Base64.getEncoder().encodeToString(IV);
            String payload = Base64.getEncoder().encodeToString(requestString.getBytes());
            String postbody = "{\"type\": \"licenseRequestExt\",\"payload\": \"" +
                    payload +
                    "\"," +
                    "\"authorizeInfo\": {\"keyAndPolicy\": [{\"distributionMode\": \"VOD\",\"keyInfo\": {\"keyId\": \"" + keyid + "\",\"key\": \"" +
                    keyString +
                    "\",\"keyEncryptedIV\": \"" +
                    IVString +
                    "\"},\"contentPolicy\": {\"securityLevel\": \"1\",\"outputControl\": \"0\",\"licenseType\": \"NONPERSISTENT\"},\"userPolicy\": {\"beginDate\": \"" + begin + "\",\"expirationDate\": \"" + expire + "\"}}], \"resultCode\": \"success\"}}";
            String originalWord = path+ XAPPID +xtimestamp+postbody;
            byte[] encryptedWordBytes;
            byte[] secretBytes = Base64.getDecoder().decode(SIGN_KEY);
            byte[] originalWordBytes = originalWord.getBytes(StandardCharsets.UTF_8);

            System.out.println(postbody);
            // Initialize the MAC object.
            Mac mac = Mac.getInstance("HmacSHA256");
            // Define the key.
            SecretKey secretKey = new SecretKeySpec(secretBytes, "HmacSHA256");
            // Initialize the key.
            mac.init(secretKey);
            // Perform encryption.
            encryptedWordBytes = mac.doFinal(originalWordBytes);
            // Use Base64 to encode bytes.
            String encryptedWord = Base64.getEncoder().encodeToString(encryptedWordBytes);

            System.out.println("key " + Arrays.toString(key));
            System.out.println("keystring " + keyString);
            System.out.println("AES " +AES);
            System.out.println("IV BASE64 " +IVString);
            System.out.println("sign key " + SIGN_KEY);
            System.out.println(encryptedWord);
            System.out.println(xtimestamp);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .header("x-appId", XAPPID)
                    .header("x-timeStamp", xtimestamp)
                    .header("x-sign", encryptedWord)
                    .POST(HttpRequest.BodyPublishers.ofString(postbody))
                    .build();
            System.out.println(request.headers());

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            System.out.println(response.body());
            //response.json.body. return to client

            apiGatewayProxyResponseEvent.setStatusCode(200);
            apiGatewayProxyResponseEvent.setBody(response.body());
            System.out.println("complete");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiGatewayProxyResponseEvent;
    }
}