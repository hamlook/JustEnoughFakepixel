package com.jef.justenoughfakepixel.core.config.utils;

import com.jef.justenoughfakepixel.utils.ColorUtils;

import java.util.Map;
import java.util.NavigableMap;

public final class StringUtils {

    private StringUtils() {}

    public static String cleanColour(String in) {
        return ColorUtils.stripColor(in);
    }

    /** Joins array elements from {@code start} onward with spaces. */
    public static String joinStrings(String[] arr, int start) {
        if (start >= arr.length) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            if (i > start) sb.append(' ');
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    /** Joins {@code arr[startInclusive..endExclusive)} with spaces. */
    public static String joinRange(String[] arr, int startInclusive, int endExclusive) {
        StringBuilder sb = new StringBuilder();
        for (int i = startInclusive; i < endExclusive; i++) {
            if (i > startInclusive) sb.append(' ');
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    /** Replaces non-breaking space variants with regular spaces and trims. */
    public static String clean(String s) {
        return s.replace('\u00A0', ' ')
                .replace('\u2007', ' ')
                .replace('\u202F', ' ')
                .trim();
    }

    /**
     * Returns the sub-map of {@code map} whose keys start with {@code prefix}.
     * Used by the config search system.
     */
    public static <T> Map<String, T> subMapWithKeysThatAreSuffixes(String prefix, NavigableMap<String, T> map) {
        return "".equals(prefix)
                ? map
                : map.subMap(prefix, true, nextString(prefix), false);
    }

    private static String nextString(String input) {
        return input.substring(0, input.length() - 1) + (char) (input.charAt(input.length() - 1) + 1);
    }
}