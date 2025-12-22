package com.nnson128.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // OTP expiration time: 5 minutes
    private static final long EXPIRATION_MINUTES = 5;
    private static final String OTP_PREFIX_FORGOT_PASSWORD = "OTP:FORGOT_PASSWORD:";
    private static final String OTP_PREFIX_REGISTER = "OTP:REGISTER:";
    private static final int OTP_LENGTH = 6;

    /**
     * Generate a random 6-digit OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates 6-digit number
        return String.valueOf(otp);
    }

    /**
     * Create OTP for forgot password
     * @param email User's email
     * @return Generated OTP
     */
    public String createForgotPasswordOtp(String email) {
        String otp = generateOtp();
        String key = OTP_PREFIX_FORGOT_PASSWORD + email;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(EXPIRATION_MINUTES));
        return otp;
    }

    /**
     * Create OTP for registration
     * @param email User's email
     * @return Generated OTP
     */
    public String createRegisterOtp(String email) {
        String otp = generateOtp();
        String key = OTP_PREFIX_REGISTER + email;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(EXPIRATION_MINUTES));
        return otp;
    }

    /**
     * Validate OTP for forgot password
     * @param email User's email
     * @param otp OTP to validate
     * @return true if valid, false otherwise
     */
    public boolean validateForgotPasswordOtp(String email, String otp) {
        String key = OTP_PREFIX_FORGOT_PASSWORD + email;
        Object storedOtp = redisTemplate.opsForValue().get(key);
        return storedOtp != null && storedOtp.toString().equals(otp);
    }

    /**
     * Validate OTP for registration
     * @param email User's email
     * @param otp OTP to validate
     * @return true if valid, false otherwise
     */
    public boolean validateRegisterOtp(String email, String otp) {
        String key = OTP_PREFIX_REGISTER + email;
        Object storedOtp = redisTemplate.opsForValue().get(key);
        return storedOtp != null && storedOtp.toString().equals(otp);
    }

    /**
     * Delete OTP for forgot password
     * @param email User's email
     */
    public void deleteForgotPasswordOtp(String email) {
        String key = OTP_PREFIX_FORGOT_PASSWORD + email;
        redisTemplate.delete(key);
    }

    /**
     * Delete OTP for registration
     * @param email User's email
     */
    public void deleteRegisterOtp(String email) {
        String key = OTP_PREFIX_REGISTER + email;
        redisTemplate.delete(key);
    }
}
