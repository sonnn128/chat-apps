package com.sonnguyen.userservice.util;

import java.util.regex.Pattern;

public class PhoneNumberUtils {
    
    private static final Pattern VIETNAM_PHONE_PATTERN = Pattern.compile("^(\\+84|84|0)(\\d{9,10})$");
    
    // Private constructor to prevent instantiation
    private PhoneNumberUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Normalize Vietnamese phone number to a consistent format
     * Examples:
     * +84799199999 -> 0799199999
     * 84799199999 -> 0799199999  
     * 0799199999 -> 0799199999
     * 
     * @param phoneNumber Raw phone number
     * @return Normalized phone number (0XXXXXXXXX format)
     */
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
        
        // If doesn't match Vietnamese pattern, return as is
        return cleaned;
    }
    
    /**
     * Check if a phone number is a valid Vietnamese phone number
     * 
     * @param phoneNumber Phone number to validate
     * @return true if valid Vietnamese phone number
     */
    public static boolean isValidVietnamesePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        String normalized = normalizeVietnamesePhone(phoneNumber);
        return normalized.matches("^0\\d{9}$");
    }
}
