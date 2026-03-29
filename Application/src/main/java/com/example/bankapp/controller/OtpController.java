package com.example.bankapp.controller;

import com.example.bankapp.OTP.otpUtil;
import com.example.bankapp.model.Account;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.service.EmailService;
import com.example.bankapp.service.SmsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/otp")
public class OtpController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SmsService smsService;

    @Autowired
    private otpUtil otpUtil;

    @Autowired
    private EmailService emailService;

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam Long accountId,
            @RequestParam String otp,
            Model model) {

        System.out.println("Verifying OTP for account ID: " + accountId);

        try { //Added a try-catch block for robust error handling
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            // Check if OTP has expired (5 minutes)
            if (account.getOtpGeneratedTime() == null
                    || account.getOtpGeneratedTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
                model.addAttribute("error", "OTP expired! Please resend a new one.");
                model.addAttribute("accountId", accountId);
                return "verify-otp";
            }

            // Check if the provided OTP matches the stored OTP
            if (otp.equals(account.getOtp())) {
                // Success: OTP is valid
                account.setVerified(true);
                account.setOtp(null); // Clear OTP after successful verification
                account.setOtpGeneratedTime(null);
                accountRepository.save(account);

                System.out.println("Account verified and saved with ID: " + account.getId());

                //Send a welcome SMS after successful verification
                String welcomeMessage = "Welcome, %s! to FAKE BANK OF INDIA. Your account with username '%s' and account ID '%d' has been created successfully.".formatted(
                        account.getAccountHolder(),
                        account.getUsername(),
                        account.getId());
                smsService.sendWelcomeSms(account.getMobileNumber(), welcomeMessage);

                // Call the sendWelcomeEmail method right here, after successful account
                // creation
                emailService.sendWelcomeEmail(account.getEmail(), account.getAccountHolder(), account.getUsername(),
                        account.getPassword());

                // Instead of a generic "success" message, we add a specific boolean variable.
                // This is what the front-end will check to trigger the redirect.
                model.addAttribute("verificationSuccess", true);
                return "verify-otp";
            }

            // If we reach this point, the OTP is incorrect
            model.addAttribute("error", "Invalid OTP!");
            model.addAttribute("accountId", accountId);
            return "verify-otp";

        } catch (RuntimeException ex) {
            // This handles the case where the accountId is not found
            model.addAttribute("error", "An error occurred during verification. Please try again.");
            model.addAttribute("accountId", accountId);
            return "verify-otp";
        }
    }

    @PostMapping("/resend")
    public String resendOtp(@RequestParam Long accountId, Model model) {

        System.out.println("Attempting to resend OTP for account ID: " + accountId);

        try {
            //Find the account. The orElseThrow handles both null and not-found accounts.
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

            //Generate a new OTP and update timestamp
            String newOtp = otpUtil.generateOtp();
            account.setOtp(newOtp);
            account.setOtpGeneratedTime(LocalDateTime.now());
            accountRepository.save(account);

            //Get and format mobile number
            String mobileNumber = account.getMobileNumber();
            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                if (!mobileNumber.startsWith("+91")) {
                    mobileNumber = "+91" + mobileNumber;
                }
                smsService.sendOtpSms(mobileNumber, newOtp);
            } else {
                System.err.println("Mobile number not found for account ID: " + accountId);
            }

            //Send OTP via Email
            try {
                emailService.sendOtpEmail(account.getEmail(), newOtp);
                System.out.println("New OTP sent successfully to " + account.getEmail());
            } catch (Exception e) {
                System.err.println("Email resend failed: " + e.getMessage());
            }


            model.addAttribute("success", "New OTP sent successfully!");
            model.addAttribute("accountId", accountId);

            return "verify-otp";

        } catch (RuntimeException ex) {
            System.err.println("Resend OTP failed due to an exception: " + ex.getMessage());
            model.addAttribute("error", "Failed to resend OTP. An error occurred.");
            model.addAttribute("accountId", accountId);
            return "verify-otp";
        }
    }
}