package medvedev.com.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@UtilityClass
@Slf4j
public class SigningUtils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String ALGORITHM_NAME = "HmacSHA256";

    public static String createSign(String text, String secretKey) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM_NAME);
        try {
            Mac mac = Mac.getInstance(ALGORITHM_NAME);
            mac.init(secretKeySpec);
            return bytesToHex(mac.doFinal(text.getBytes()));
        } catch (Exception ex) {
            log.debug("Error crete sign", ex);
            return "";
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
