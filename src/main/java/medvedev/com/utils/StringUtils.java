package medvedev.com.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public static String transformTgMessage(String string) {
        return string.replaceAll("[_*]?(\\n)?", "");
    }
}
