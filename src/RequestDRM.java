import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

public class RequestDRM {
    static String path = "/drmproxy/v2/getLicense";
    static String xappid = "TestForDeveloper";
    static String xtimestamp = Long.toString(new Date().getTime());
    static String begin = Long.toString(new Date().getTime()/1000);
    static String expire = Long.toString(new Date().getTime()/1000 + 86400);
    static String postbody = "{\"type\": \"licenseRequestExt\",\"payload\": \"eyJjZXJ0aWZpY2F0ZUNoYWluIjpbIk1JSUQ1RENDQXN5Z0F3SUJBZ0lVQVFJQUFBQVE5MDQ5N0ZKVUlmc1JWem40dnJvd0RRWUpLb1pJaHZjTkFRRUxCUUF3TWpFTE1Ba0dBMVVFQmd3Q1ZWTXhEVEFMQmdOVkJBb01CRU5FVkVFeEZEQVNCZ05WQkFNTUMwUmxkbWxqWlNCRFFTQXlNQjRYRFRJd01EWXlNakl4TXpNek5Gb1hEVFF3TURZeU1qSXhNek16TkZvd2daSXhDekFKQmdOVkJBWU1Ba05PTVE4d0RRWURWUVFLREFaSWRXRjNaV2t4RXpBUkJnTlZCQXNNQ2toMVlYZGxhU0JEUWtjeEtqQW9CZ05WQkFNTUlVVklMVVJTVFVNdFJFTkVMVVZJTFVoVlFWY3RNakF5TURBeUxUQXlMVUZPUVRFeE1DOEdBMVVFQlF3b01EWTNPVGN3T1dRd01URTBOR1JrWldVelpUaGhaV05tWVRKall6azJaR00wWkdGa1ptTmtaakNDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFLRTJDdnZ2cTdpRk9ZMGtWZTRaREIwSVAzNnJjOWErY0xBK3liaTJpTkxPVmhjWWpQSE9Xb1VjdDVoelFLOTV4YnI3TCt2NTBWcXd6L3BSczVRUU5LZTVnQnZzV2NzZGFNS29uSGRoV2o5amFyMWozQ3YyMFN5aThsdi9jTyswTEpIR2ZnNklhbnBjc1FUTUdFNXREWG1hNjdRVE5UTnM1OGZ2VFJ0elNHeVRKUkZmMWIxQVZiWWdkbVlPM3JlemQ1cFZWdzF0SjNuUmVEOXFERXFQM1dVZkhkWEJnTEVlZE9pQ0tTaG52cWxwNCtCOWlXOStGcFRUd1VLRnpyeEs1ZzZTdS9CYlNMT3k1TnBpYmZLcDFIMlRLdXNSSFE2RVpZZ2tuZ2djU0Rhbi9HdFhDb0psdlAxVUU2aGJhR2ZRU3IyeGlTclgzWFMxUk5UZlZPVklXYWNDQXdFQUFhT0JrRENCalRBT0JnTlZIUThCQWY4RUJBTUNCYUF3SHdZRFZSMGpCQmd3Rm9BVVJjYm9SR2JQWHJlZXZkQkRnSVM5VDcvSDZoNHdGUVlEVlIwbEFRSC9CQXN3Q1FZSEtvRWNodTh3QlRCREJnTlZIUjhFUERBNk1EaWlOcVEwTURJeEN6QUpCZ05WQkFZTUFrTk9NUTB3Q3dZRFZRUUtEQVJEUkZSQk1SUXdFZ1lEVlFRRERBdEVaWFpwWTJVZ1EwRWdNVEFOQmdrcWhraUc5dzBCQVFzRkFBT0NBUUVBQjhhdFExeGtKR05Xay8zeEhLeEcrV0lLMXRiQVl0cE82RlM3Z0NtOE1idVlyZDZVbzhocXhsMWZnZTh2M25CdjZqemVKbXN3RjZSbXM5bGV2emU2L1g2YkxldzdmcVdnVG5RTzMzejhMYkFtNWRHL2t3YW5tZjI0akYvRnVKWGlHajloZms3dXRpZWlPcmVZTGJTdmFmVVUzNjVrNENTeW5ieDhPQ1NYMndiZ1k4QVpCSWJFMTB5UXZsWXFyclF0b3l3N3RSTnRWY05YOTZMa1pmMDVPaWROQ0hCZzY1K3ZsajVUblptSVIwR24xemplbVRDNXNWN0hFWHhmdHMxejJKcHJ4VVlTaTFRQjIrQ1M5RCt2VnVEcTd1QWtZbitERGVGdG5vd1Evb1lXSEpOOVVibnJ4VWdIdnVxYXpVU3FmL3ozODlQTS8rUHViUExNMGdiTFlRPT0iLCJNSUlEUmpDQ0FpNmdBd0lCQWdJRVNEbWZSakFOQmdrcWhraUc5dzBCQVFzRkFEQXVNUXN3Q1FZRFZRUUdEQUpEVGpFTk1Bc0dBMVVFQ2d3RVEwUlVRVEVRTUE0R0ExVUVBd3dIVW05dmRDQkRRVEFnRncweU1EQTJNRGN4TmpBd01EQmFHQTh5TURjd01EWXdPREUxTlRrMU9Wb3dNakVMTUFrR0ExVUVCZ3dDVlZNeERUQUxCZ05WQkFvTUJFTkVWRUV4RkRBU0JnTlZCQU1NQzBSbGRtbGpaU0JEUVNBeU1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBeC9HZHBVakx4WCtPTCtYSlpiOWVYd0FpdnRxWHNPOW95R3BNVFBYR3RVbFZsbDFFbmVDc0k2YU1kNEN2RkhnbDNPcXpsdUVDMWIxcUNVNHZ4dVNOS3ozMUtmUDVIUmV3cGI3Zmd4VkRGVmRDbHFXRGYzTk93SWVtQ2JYQ2ZraFhIc1dJMGh2WkZuVXBrM2wvQitoendHbHNMY3crVkFUMG5OYVovS3BoenFPYS9RZlZVbmFJWG9aYk8waVpwR0FmYkJiYWZtY3c5U0ZXemY2eHE3WHFBdTJ6L2lJSW82NzBlQ3BVWFQxaEpKVmpqRUFKVGZ5c0pVdERrQjRCZWdQNG5EbzFMZGRHdEhBMlFEeFR6NUdETktDUHNZNWJZL1UwRTFmLzFkRDVPaDl2NmVDRVhPQm5vTGRCTDBRQTFudnNVeGhyMWZ3N3FkQVlpUDVTaVc0dUNRSURBUUFCbzJZd1pEQWRCZ05WSFE0RUZnUVVSY2JvUkdiUFhyZWV2ZEJEZ0lTOVQ3L0g2aDR3RGdZRFZSMFBBUUgvQkFRREFnRUdNQklHQTFVZEV3RUIvd1FJTUFZQkFmOENBUUF3SHdZRFZSMGpCQmd3Rm9BVWR6bm90WlhxSVJOSm54RTRpUXY3TUJmRmRSNHdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBRVYrTGdXQkw1QW1sNTlzeXFYbkU0OHQwWEIxOVQ3djNhNXpLZ2YwMmlMVmdpeWVheU1WYWNGdVBCTXd2d0JiOVhqUWVVb09VVzBpNW5GMHk1cXVlS0VvcTBaVW9wbXRveE5EMi82a3loMHorcGRrZE5NK1pEVjI0Ny9OOWlrWTkwbE10WkV6R2Y0bVgydnFlL0pibjRGTnJMMzlxNndhQkdvZjR1V2FnSU9nNDdEVGF5UEprRmtKeHFCWi9TeVRRZFVKTmpNMzAyMjJSQ0JVbDdHem9nNDFMWXQvTlRGL0wrL0xiOHNrNmxUWXNBaXZDLzZZeHVNajhjbUxxb1pYLy80KzRTZGhSOG1tNDNFTlBXYXZXWWM0RGE1Q2xmM3U2cnd3THpLR0w0MVNQZG00VmtIdlp0di9kaHA1TTRjZytpYWhveEQzNHZRWmNsTXdNa0VnYmwwPSJdLCJub25jZSI6IjJuN0Z6RW1xd09mTTNDWXh1UFA2WXc9PSIsInR5cGUiOiJsaWNlbnNlUmVxdWVzdCIsInZlcnNpb24iOiIyLjAiLCJyZXF1ZXN0VGltZSI6IjE1OTk2Nzg2OTMiLCJkZXZpY2VJRCI6IkJubHduUUVVVGQ3ajZLN1Bvc3lXM0UydC9OOD0iLCJzdXBwb3J0ZWRBbGdvcml0aG1zIjpbIktNU1Byb2ZpbGU0Il0sImNvbnRlbnRJRHMiOlsiQUFBQXEzQnpjMmdBQUFBQVBWNXROWnVhUWVpNFE5MDhibkxFTEFBQUFJdDdJbU52Ym5SbGJuUkpSQ0k2SWxac1ZqUlJhM1F5WlVWd1ZrMUZPVk5hYmtreVpXdE9kVTE2U214a2VqQTVJaXdpWlc1elkyaGxiV0VpT2lKRFJVNURJaXdpYTJsa2N5STZXeUpaVjBWM1dtcE9iVTVVWXpSWlZHUnJUa2RGZDA1SFJUTlpNbEV5V2tSU2EwMXFaRzFOZWswelQxUnJQU0pkTENKMlpYSnphVzl1SWpvaVZqRXVNQ0o5Il0sInNpZ25hdHVyZSI6ImVSN2grRXFIenl6MkJoQ0w0UFdILzkzblhrYXhtVlJOUkNVQjNFQ2NpTlcyTWpCa1d0YTRzbTlXdUYxUDVtWTdTTjNRK3Z4OU53V2JRSXpHdkdSbDJ1SVdFYjhvdi81NXBPTFJwTy92UFJCbm5iekZ0SHNGWkJZbDk0NG9IRTVoOGRMQjh1WHhySEZtWm9CazJobW5kbEREc2p1SjFHSy9Jdm16clF3VHFzaXdwTFNUMnVhQUFJbk9RT3hTWkFlMEVSemlIZ1JHMFNHalhySlE4ZnRCbE5PbHB4ZVBmOHV4ZFZQVCswSThPNi85UWt0N0tDeEFSTmNQYkxhSll4cjUveHRyRFVKTUNtU0plbExEa0tEU3RFNU9WeUQzQXU4SzVDdFZiZXcvNVpNTmFoQkROeWZKT0RmQnI0emt0cVZPMGFhTFY4MENyUXprS0pScSs0ZWxKZz09In0=\",\"authorizeInfo\": [{\"keyAndPolicy\": [{\"distributionMode\": \"VOD\",\"keyInfo\": [{\"keyId\": \"aa0f3f578a7d4a04a7cd6d4d27f33799\",\"key\": \"Mr1T84XFsXHE6vl5ZeiHheT8G23oDbtAdVf6HvGSZaI=\",\"keyEncryptedIV\": \"NYoxOK8mVqPlHwCmu4zBTw==\"}],\"contentPolicy\": [{\"securityLevel\": \"1\",\"outputControl\": \"0\",\"licenseType\": \"NONPERSISTENT\"}],\"UserPolicy\": [{\"beginDate\": \"" + begin + "\",\"expirationDate\": \"" + expire + "\"}]}],\"contentid\": \"VUxBKvxJU0ORfr6zCn32ew==\"}]}";
    static String signKey = "VAv4XeXRNpmZEwJYQ878J5lNCbmxZpxwU2z57wmbYnA=";
    static String originalWord = path+xappid+xtimestamp+postbody;
    static byte[] encryptedWordBytes;

    public static void main(String[] args) throws IOException, InterruptedException {
        byte[] secretBytes = Base64.getDecoder().decode(signKey);
        byte[] originalWordBytes = originalWord.getBytes("UTF-8");

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

            System.out.println(encryptedWord);
            System.out.println(xtimestamp);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("HTTPS://wiseplay.cloud.huawei.com/drmproxy/v2/getLicense"))
//                    .uri(URI.create("https://drmkit.hwcloudtest.cn:8080/drmproxy/v2/getLicense"))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
//                    .header("x-appId", "")
                    .header("x-appId", xappid)
                    .header("x-timeStamp", xtimestamp)
                    .header("x-sign", encryptedWord)
//                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .POST(HttpRequest.BodyPublishers.ofString(postbody))
                    .build();
            System.out.println(request.headers());
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            System.out.println(response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("end");
    }

    public RequestDRM() throws UnsupportedEncodingException {
    }
}
