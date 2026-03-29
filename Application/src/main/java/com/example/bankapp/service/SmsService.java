package com.example.bankapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

@Service
public class SmsService {
    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromNumber;

    public void sendOtpSms(String mobileNumber, String otp) {
        try {
            Twilio.init(accountSid, authToken);

            //Ensure format (add +91 for India if not present)
            String formattedNumber = mobileNumber.startsWith("+") ? mobileNumber : "+91" + mobileNumber;

            Message.creator(
                    new com.twilio.type.PhoneNumber(formattedNumber),
                    new com.twilio.type.PhoneNumber(fromNumber),
                    "Your OTP is: " + otp + " (Valid for 5 minutes)")
                    .create();

            System.out.println("OTP sent to " + formattedNumber);
        } catch (Exception e) {
            System.err.println("Failed to send OTP to " + mobileNumber);
            e.printStackTrace();
        }
    }

    public void sendWelcomeSms(String mobileNumber, String message) {
        try {
            Twilio.init(accountSid, authToken);
            String formattedNumber = mobileNumber.startsWith("+") ? mobileNumber : "+91" + mobileNumber;

            Message.creator(
                    new com.twilio.type.PhoneNumber(formattedNumber),
                    new com.twilio.type.PhoneNumber(fromNumber),
                    message)
                    .create();

            System.out.println("Welcome SMS sent to " + formattedNumber);
        } catch (Exception e) {
            System.err.println("Failed to send welcome SMS to " + mobileNumber);
            e.printStackTrace();
        }
    }
}
