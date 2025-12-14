package com.nnson128.userservice.util;

import java.util.regex.Pattern;

public class PhoneNumberUtils {

    private static final Pattern VIETNAM_PHONE_PATTERN = Pattern.compile("^(\\+84|84|0)(\\d{9,10})$");

    private PhoneNumberUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String normalizeVietnamesePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }

        // Remove all spaces and special characters except +
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Check if it matches Vietnamese phone pattern
        if (VIETNAM_PHONE_PATTERN.matcher(cleaned).matches()) {
            if (cleaned.startsWith("+84")) {
                // +84XXXXXXXXX -> 0XXXXXXXXX
                return "0" + cleaned.substring(3);
            } else if (cleaned.startsWith("84")) {
                // 84XXXXXXXXX -> 0XXXXXXXXX
                return "0" + cleaned.substring(2);
            } else if (cleaned.startsWith("0")) {
                // 0XXXXXXXXX -> 0XXXXXXXXX (already normalized)
                return cleaned;
            }
        }

        return cleaned;
    }

    public static boolean isValidVietnamesePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        String normalized = normalizeVietnamesePhone(phoneNumber);
        return normalized.matches("^0\\d{9}$");
    }
}
