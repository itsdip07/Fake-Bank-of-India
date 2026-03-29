package com.example.bankapp.service;

import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail);
            e.printStackTrace();
        }
    }

    public void sendStatementEmail(String toEmail, String accountHolder, byte[] pdfBytes, String fileName) {
        try {
            // Create a MimeMessage to handle attachments
            MimeMessage message = mailSender.createMimeMessage();

            // The 'true' flag indicates this is a multipart message (contains attachments)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Account Statement - Fake Bank Of India");

            String emailBody = "Dear " + accountHolder + ",\n\n"
                    + "Please find attached your account transaction statement.\n\n"
                    + "Thank you for banking with Fake Bank Of India!";
            helper.setText(emailBody);

            // Attach the PDF byte array
            helper.addAttachment(fileName, new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            System.out.println("Statement email sent successfully to " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send statement email to " + toEmail);
            e.printStackTrace();
        }
    }
    
    public void sendDebitEmail(String toEmail, String accountHolder, java.math.BigDecimal amount,
            java.math.BigDecimal currentBalance, String description) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Debit Alert: Fake Bank Of India");

        String emailBody = "Dear " + accountHolder + ",\n\n"
                + "An amount of Rs." + amount + " has been DEBITED from your account.\n"
                + "Transaction Info: " + description + "\n"
                + "Available Balance: Rs." + currentBalance + "\n\n"
                + "If you did not authorize this transaction, please contact our support team immediately.\n\n"
                + "Sincerely,\n"
                + "The Fake Bank Of India Team";

        message.setText(emailBody);

        try {
            mailSender.send(message);
            System.out.println("Debit email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send debit email to " + toEmail);
            e.printStackTrace();
        }
    }

    public void sendCreditEmail(String toEmail, String accountHolder, java.math.BigDecimal amount,
            java.math.BigDecimal currentBalance, String description) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Credit Alert: Fake Bank Of India");

        String emailBody = "Dear " + accountHolder + ",\n\n"
                + "An amount of Rs." + amount + " has been CREDITED to your account.\n"
                + "Transaction Info: " + description + "\n"
                + "Available Balance: Rs." + currentBalance + "\n\n"
                + "Thank you for banking with us!\n\n"
                + "Sincerely,\n"
                + "The Fake Bank Of India Team";

        message.setText(emailBody);

        try {
            mailSender.send(message);
            System.out.println("Credit email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send credit email to " + toEmail);
            e.printStackTrace();
        }
    }

    public void sendWelcomeEmail(String toEmail, String accountHolder, String username, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to Fake Bank Of India!");

        // The body of the welcome email
        String emailBody = "Dear " + accountHolder + ",\n\n"
                + "Welcome to Fake Bank Of India! We're thrilled to have you join our community.\n\n"
                + "Our app is designed to make your banking experience simple, secure, and convenient. "
                + "You can easily manage your accounts, transfer funds, view transactions, and much more.\n\n"
                + "Here are your login details:\n\n"
                + "Login Details:\n"
                + "Username: " + username + "\n"
                + "Password: (Not Shown due to Security reasons), if you forgot your Password please Contact Support\n\n"
                + "If you have any questions or need assistance, please don't hesitate to reach out to our support team.\n\n"
                + "Thank you for choosing Fake Bank Of India!\n\n"
                + "Sincerely,\n"
                + "The Fake Bank Of India Team";

        message.setText(emailBody);

        try {
            mailSender.send(message);
            System.out.println("Welcome email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to " + toEmail);
            e.printStackTrace();
        }
    }
}