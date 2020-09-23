import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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

import static sun.security.jca.JCAUtil.getSecureRandom;

public class RequestDRM {
    static final String AES = "QoHOSmSPUH1uGPr/lsxWb+1Mslu7cnJ9+tvPmNfEyJs=";
    static final String XAPPID = "736430079244616618";
    static final byte[] IV = buildRandomBytes(16);
    static final byte[] CONTENTKEY = buildRandomBytes(32);
    static final String SIGN_KEY = "NuNo4KniGniwrTZjs5fQcHeRCQsJ61H84UXUdaiLzIY=";

    public static void main(String[] args) throws Exception {
        String path = "/drmproxy/v2/getLicense";
        String xtimestamp = Long.toString(new Date().getTime());
        String begin = Long.toString(new Date().getTime()/1000);
        String expire = Long.toString(new Date().getTime()/1000 + 86400);
        byte[] key = doCipher(1, CONTENTKEY, Base64.getDecoder().decode(AES), IV);
        String keyString = Base64.getEncoder().encodeToString(key);
        String IVString = Base64.getEncoder().encodeToString(IV);
        String payload = "abcde=";
        String postbody = "{\"type\": \"licenseRequestExt\",\"payload\": \"" +
                payload +
                "\"," +
                "\"authorizeInfo\": {\"keyAndPolicy\": [{\"distributionMode\": \"VOD\",\"keyInfo\": {\"keyId\": \"aa0f3f578a7d4a04a7cd6d4d27f33799\",\"key\": \"" +
                keyString +
                "\",\"keyEncryptedIV\": \"" +
                IVString +
                "\"},\"contentPolicy\": {\"securityLevel\": \"1\",\"outputControl\": \"0\",\"licenseType\": \"NONPERSISTENT\"},\"userPolicy\": {\"beginDate\": \"" + begin + "\",\"expirationDate\": \"" + expire + "\"}}],\"contentid\": \"VUxBKvxJU0ORfr6zCn32ew==\",\"resultCode\": \"success\"}}";
        String originalWord = path+ XAPPID +xtimestamp+postbody;
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
            System.out.println("IV " +IVString);
            System.out.println("sign key " + SIGN_KEY);



            System.out.println(encryptedWord);
            System.out.println(xtimestamp);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("HTTPS://wiseplay.cloud.huawei.com/drmproxy/v2/getLicense"))
//                    .uri(URI.create("https://drmkit.hwcloudtest.cn:8080/drmproxy/v2/getLicense"))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
//                    .header("x-appId", "")
                    .header("x-appId", XAPPID)
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

    public static byte[] doCipher(int cipherMode, byte[] wordBytes, byte[] secretBytes, byte[] ivBytes) throws Exception {
        // 定义密钥机制对象
        SecretKeySpec sKeySpec;
        AlgorithmParameterSpec ivSpec;

        // 定义加密对象
        Cipher cipher;

        // 定义加解密输出结果
            // 初始化加密密钥对象
            sKeySpec = new SecretKeySpec(secretBytes, "AES");

            // 初始化IV对象
            ivSpec = new IvParameterSpec(ivBytes);

            // 初始化加密对象
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // 加载密钥IV加密对象
            cipher.init(cipherMode, sKeySpec, ivSpec);

            // 进行加解密操作
            return cipher.doFinal(wordBytes);


        // 返回输出的字节数组
    }
    public static byte[] buildRandomBytes(int i) {
        // 获取格式化的字节长度信息

        // 构造安全随机数对象
        SecureRandom random = getSecureRandom();

        // 定义随机字节数组对象
        byte[] randomBytes = new byte[i];

        // 构建随机字节数
        random.nextBytes(randomBytes);

        // 返回随机数字节对象
        return randomBytes;
    }

}
