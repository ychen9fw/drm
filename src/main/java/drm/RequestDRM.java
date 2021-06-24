package drm;

import com.amazonaws.services.lambda.runtime.events.CloudFrontEvent;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
//import static sun.security.jca.JCAUtil.getSecureRandom;

public class RequestDRM {
    static final byte[] IV = Base64.getDecoder().decode("wuK1dCnuvE+gDRoWOkLKmQ==");//buildRandomBytes(16);
//    static final byte[] IV = Base64.getDecoder().decode("m19mbITKiqiMvpap5MNsMA==");//buildRandomBytes(16);
    static final byte[] CONTENTKEY = hexStringToByteArray("9747E321569144477B7705C7DAE3024D");//buildRandomBytes(32);
//    static final byte[] CONTENTKEY = hexStringToByteArray("8aa08e0981a74f1fa26ecc34285edc0a");//buildRandomBytes(32);

    static final String URL = "HTTPS://wiseplay.cloud.huawei.com/drmproxy/v2/getLicense";
    static final String AES = "QoHOSmSPUH1uGPr/lsxWb+1Mslu7cnJ9+tvPmNfEyJs=";
    static final String XAPPID = "736430079244616618";
    static final String XPORTALID = "";
    static final String SIGN_KEY = "NuNo4KniGniwrTZjs5fQcHeRCQsJ61H84UXUdaiLzIY=";

//    static final String URL = "HTTPS://wiseplay.cloud.huawei.com/drmproxy/v3/getLicense";
//    static final String XPORTALID = "736430079244616618";
//    static final String XAPPID = "102808471";

//    static final String URL = "https://drmkit.hwcloudtest.cn:8080/drmproxy/v2/getLicense";
//    static final String AES = "GujM0OeYXMC2IDpVhVoQNK/CUpgOlwypDlICaJ+Uerk=";
//    static final String XAPPID = "TestForDeveloper";
//    static final String SIGN_KEY = "VAv4XeXRNpmZEwJYQ878J5lNCbmxZpxwU2z57wmbYnA=";


    public static void main(String[] args) throws Exception {
        //receive "payload" from client
        String path = "/drmproxy/v2/getLicense";
//        String path = "/drmproxy/v3/getLicense";
        String begin = Long.toString(new Date().getTime()/1000);
        String xtimestamp = Long.toString(new Date().getTime());
        String expire = Long.toString(new Date().getTime()/1000 + 86400);
        byte[] key = doCipher(1, CONTENTKEY, Base64.getDecoder().decode(AES), IV);

        String keyString = Base64.getEncoder().encodeToString(key);
        String IVString = Base64.getEncoder().encodeToString(IV);
        String payload = "eyJjZXJ0aWZpY2F0ZUNoYWluIjpbIk1JSUQ1RENDQXN5Z0F3SUJBZ0lVQVFJQUFBQVE5MDQ5N0ZKVUlmc1JWem40dnJvd0RRWUpLb1pJaHZjTkFRRUxCUUF3TWpFTE1Ba0dBMVVFQmd3Q1ZWTXhEVEFMQmdOVkJBb01CRU5FVkVFeEZEQVNCZ05WQkFNTUMwUmxkbWxqWlNCRFFTQXlNQjRYRFRJd01EWXlNakl4TXpNek5Gb1hEVFF3TURZeU1qSXhNek16TkZvd2daSXhDekFKQmdOVkJBWU1Ba05PTVE4d0RRWURWUVFLREFaSWRXRjNaV2t4RXpBUkJnTlZCQXNNQ2toMVlYZGxhU0JEUWtjeEtqQW9CZ05WQkFNTUlVVklMVVJTVFVNdFJFTkVMVVZJTFVoVlFWY3RNakF5TURBeUxUQXlMVUZPUVRFeE1DOEdBMVVFQlF3b01EWTNPVGN3T1dRd01URTBOR1JrWldVelpUaGhaV05tWVRKall6azJaR00wWkdGa1ptTmtaakNDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFLRTJDdnZ2cTdpRk9ZMGtWZTRaREIwSVAzNnJjOWErY0xBK3liaTJpTkxPVmhjWWpQSE9Xb1VjdDVoelFLOTV4YnI3TCt2NTBWcXd6L3BSczVRUU5LZTVnQnZzV2NzZGFNS29uSGRoV2o5amFyMWozQ3YyMFN5aThsdi9jTyswTEpIR2ZnNklhbnBjc1FUTUdFNXREWG1hNjdRVE5UTnM1OGZ2VFJ0elNHeVRKUkZmMWIxQVZiWWdkbVlPM3JlemQ1cFZWdzF0SjNuUmVEOXFERXFQM1dVZkhkWEJnTEVlZE9pQ0tTaG52cWxwNCtCOWlXOStGcFRUd1VLRnpyeEs1ZzZTdS9CYlNMT3k1TnBpYmZLcDFIMlRLdXNSSFE2RVpZZ2tuZ2djU0Rhbi9HdFhDb0psdlAxVUU2aGJhR2ZRU3IyeGlTclgzWFMxUk5UZlZPVklXYWNDQXdFQUFhT0JrRENCalRBT0JnTlZIUThCQWY4RUJBTUNCYUF3SHdZRFZSMGpCQmd3Rm9BVVJjYm9SR2JQWHJlZXZkQkRnSVM5VDcvSDZoNHdGUVlEVlIwbEFRSC9CQXN3Q1FZSEtvRWNodTh3QlRCREJnTlZIUjhFUERBNk1EaWlOcVEwTURJeEN6QUpCZ05WQkFZTUFrTk9NUTB3Q3dZRFZRUUtEQVJEUkZSQk1SUXdFZ1lEVlFRRERBdEVaWFpwWTJVZ1EwRWdNVEFOQmdrcWhraUc5dzBCQVFzRkFBT0NBUUVBQjhhdFExeGtKR05Xay8zeEhLeEcrV0lLMXRiQVl0cE82RlM3Z0NtOE1idVlyZDZVbzhocXhsMWZnZTh2M25CdjZqemVKbXN3RjZSbXM5bGV2emU2L1g2YkxldzdmcVdnVG5RTzMzejhMYkFtNWRHL2t3YW5tZjI0akYvRnVKWGlHajloZms3dXRpZWlPcmVZTGJTdmFmVVUzNjVrNENTeW5ieDhPQ1NYMndiZ1k4QVpCSWJFMTB5UXZsWXFyclF0b3l3N3RSTnRWY05YOTZMa1pmMDVPaWROQ0hCZzY1K3ZsajVUblptSVIwR24xemplbVRDNXNWN0hFWHhmdHMxejJKcHJ4VVlTaTFRQjIrQ1M5RCt2VnVEcTd1QWtZbitERGVGdG5vd1Evb1lXSEpOOVVibnJ4VWdIdnVxYXpVU3FmL3ozODlQTS8rUHViUExNMGdiTFlRPT0iLCJNSUlEUmpDQ0FpNmdBd0lCQWdJRVNEbWZSakFOQmdrcWhraUc5dzBCQVFzRkFEQXVNUXN3Q1FZRFZRUUdEQUpEVGpFTk1Bc0dBMVVFQ2d3RVEwUlVRVEVRTUE0R0ExVUVBd3dIVW05dmRDQkRRVEFnRncweU1EQTJNRGN4TmpBd01EQmFHQTh5TURjd01EWXdPREUxTlRrMU9Wb3dNakVMTUFrR0ExVUVCZ3dDVlZNeERUQUxCZ05WQkFvTUJFTkVWRUV4RkRBU0JnTlZCQU1NQzBSbGRtbGpaU0JEUVNBeU1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBeC9HZHBVakx4WCtPTCtYSlpiOWVYd0FpdnRxWHNPOW95R3BNVFBYR3RVbFZsbDFFbmVDc0k2YU1kNEN2RkhnbDNPcXpsdUVDMWIxcUNVNHZ4dVNOS3ozMUtmUDVIUmV3cGI3Zmd4VkRGVmRDbHFXRGYzTk93SWVtQ2JYQ2ZraFhIc1dJMGh2WkZuVXBrM2wvQitoendHbHNMY3crVkFUMG5OYVovS3BoenFPYS9RZlZVbmFJWG9aYk8waVpwR0FmYkJiYWZtY3c5U0ZXemY2eHE3WHFBdTJ6L2lJSW82NzBlQ3BVWFQxaEpKVmpqRUFKVGZ5c0pVdERrQjRCZWdQNG5EbzFMZGRHdEhBMlFEeFR6NUdETktDUHNZNWJZL1UwRTFmLzFkRDVPaDl2NmVDRVhPQm5vTGRCTDBRQTFudnNVeGhyMWZ3N3FkQVlpUDVTaVc0dUNRSURBUUFCbzJZd1pEQWRCZ05WSFE0RUZnUVVSY2JvUkdiUFhyZWV2ZEJEZ0lTOVQ3L0g2aDR3RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZEV3RUIvd1FJTUFZQkFmOENBUUF3SHdZRFZSMGpCQmd3Rm9BVWR6bm90WlhxSVJOSm54RTRpUXY3TUJmRmRSNHdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBRVYrTGdXQkw1QW1sNTlzeXFYbkU0OHQwWEIxOVQ3djNhNXpLZ2YwMmlMVmdpeWVheU1WYWNGdVBCTXd2d0JiOVhqUWVVb09VVzBpNW5GMHk1cXVlS0VvcTBaVW9wbXRveE5EMi82a3loMHorcGRrZE5NK1pEVjI0Ny9OOWlrWTkwbE10WkV6R2Y0bVgydnFlL0pibjRGTnJMMzlxNndhQkdvZjR1V2FnSU9nNDdEVGF5UEprRmtKeHFCWi9TeVRRZFVKTmpNMzAyMjJSQ0JVbDdHem9nNDFMWXQvTlRGL0wrL0xiOHNrNmxUWXNBaXZDLzZZeHVNajhjbUxxb1pYLy80KzRTZGhSOG1tNDNFTlBXYXZXWWM0RGE1Q2xmM3U2cnd3THpLR0w0MVNQZG00VmtIdlp0di9kaHA1TTRjZytpYWhveEQzNHZRWmNsTXdNa0VnYmwwPSJdLCJub25jZSI6InhpVFhsWExVdDBEei8vdVB1dU56THc9PSIsInR5cGUiOiJsaWNlbnNlUmVxdWVzdCIsInZlcnNpb24iOiIyLjAiLCJyZXF1ZXN0VGltZSI6IjE2MDA3OTMxNTMiLCJkZXZpY2VJRCI6IkJubHduUUVVVGQ3ajZLN1Bvc3lXM0UydC9OOD0iLCJzdXBwb3J0ZWRBbGdvcml0aG1zIjpbIktNU1Byb2ZpbGU0Il0sImNvbnRlbnRJRHMiOlsiQUFBQXEzQnpjMmdBQUFBQVBWNXROWnVhUWVpNFE5MDhibkxFTEFBQUFJdDdJbU52Ym5SbGJuUkpSQ0k2SWxac1ZqUlJhM1F5WlVWd1ZrMUZPVk5hYmtreVpXdE9kVTE2U214a2VqQTVJaXdpWlc1elkyaGxiV0VpT2lKRFJVNURJaXdpYTJsa2N5STZXeUpOVkUxNVdYcFplVTFxVFRWWk1sWnRUa1JKZUU1Nlp6Tk5la0V4VFdwSk5Ga3lWbTFhUkUxNVdWUmpQU0pkTENKMlpYSnphVzl1SWpvaVZqRXVNQ0o5Il0sInNpZ25hdHVyZSI6IkZjNHVweURXeUFSR3RzcWYzTFBtYmdVQzNmRk54OVdhQS8wR0NGR0UrVEZNeURxYTN1eWpjVUNPRTVhL3ZUaHlnTGhZenNiUWpxMmFJQUVtMGZ2cUhwc3hteDhnalUvbHpuWTUrU282OVdpZzZXNUNpNG9FZXNya1FnVTRhUzE4Q1BMQm5nb0svUSt3SDBGdTZEa1hzZWdGKzR4WUFXeTM3ZWtvUklucDJ6NU9Oa0FWeDBRYnh3UTZ3SEtjcWtFQVk4R2padmViR0ZueUhqRVZlR25iR1ozZU1hM1VCM1NCd09yNDYyR0ZJRTZFTWlyUXZpYnNwNHdNdURhWHRhdkRsM1RtZEU4ZlQwbjN0U1ZpUWJQYVVIb2xXTUJHVmthdEdCaHVzbGZnWnBickpNWnlvS1FBUElQZG9PdS82TjdIbTI2TFM3aHNkL2Q5NFNmdE9FYk5pQT09In0=";
        String postbody = "{\"type\": \"licenseRequestExt\",\"payload\": \"" +
                payload +
                "\"," +
                "\"authorizeInfo\": {\"keyAndPolicy\": [{\"distributionMode\": \"VOD\",\"keyInfo\": {\"keyId\": \"aa0f3f578a7d4a04a7cd6d4d27f33799\",\"key\": \"" +
                keyString +
                "\",\"keyEncryptedIV\": \"" +
                IVString +
                "\"},\"contentPolicy\": {\"securityLevel\": \"1\",\"outputControl\": \"0\",\"licenseType\": \"NONPERSISTENT\"},\"userPolicy\": {\"beginDate\": \"" + begin + "\",\"expirationDate\": \"" + expire + "\"}}],\"contentid\": \"VUxBKvxJU0ORfr6zCn32ew==\",\"resultCode\": \"success\"}}";
        String originalWord = path + XAPPID + XPORTALID + xtimestamp+postbody;
        byte[] encryptedWordBytes;
        byte[] secretBytes = Base64.getDecoder().decode(SIGN_KEY);
        byte[] originalWordBytes = originalWord.getBytes(StandardCharsets.UTF_8);

        System.out.println(postbody);
        try {
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

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okclient = new OkHttpClient().newBuilder().addInterceptor(logging).build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, postbody);
            Request okrequest = new Request.Builder()
                    .url(URL)
                    .method("POST", body)
                    .addHeader("x-appId", XAPPID)
//                    .addHeader("x-portalId", XPORTALID)
                    .addHeader("x-timeStamp", xtimestamp)
                    .addHeader("x-sign", encryptedWord)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response okresponse = okclient.newCall(okrequest).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("end");
    }

    public RequestDRM() throws UnsupportedEncodingException {
    }

    public static byte[] doCipher(int cipherMode, byte[] wordBytes, byte[] secretBytes, byte[] ivBytes) throws Exception {
        SecretKeySpec sKeySpec;
        AlgorithmParameterSpec ivSpec;

        Cipher cipher;

            sKeySpec = new SecretKeySpec(secretBytes, "AES");

            ivSpec = new IvParameterSpec(ivBytes);

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(cipherMode, sKeySpec, ivSpec);

            return cipher.doFinal(wordBytes);


    }


    public static byte[] buildRandomBytes(int i) {
        byte[] b = new byte[i];
        new Random().nextBytes(b);
        return b;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
