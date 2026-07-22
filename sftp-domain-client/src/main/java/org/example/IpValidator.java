package org.example;

import java.util.regex.Pattern;

public final class IpValidator {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])" +
                    "(\\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}$"
    );

    private IpValidator() {
    }

    public static boolean isValidIPv4(String value) {
        return value != null && IPV4_PATTERN.matcher(value.trim()).matches();
    }
}