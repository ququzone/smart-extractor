package edu.nwnu.ququzone.extractor.utlis;

/**
 * String utls.
 *
 * @author Yang XuePing
 */
public final class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }
}
