package com.example.bankapp.OTP;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class otpUtil {
    public String generateOtp() {
        Random random = new Random();
        return "%06d".formatted(random.nextInt(999999));
    }
}
