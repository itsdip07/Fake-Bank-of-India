package com.example.bankapp.controller;

import com.example.bankapp.model.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.awt.*;
import java.util.List;
import java.io.ByteArrayOutputStream;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Beneficiary;
import com.example.bankapp.service.AccountService;
import com.example.bankapp.service.BeneficiaryService;
import com.example.bankapp.service.EmailService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class BankController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BeneficiaryService beneficiaryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findByUsername(username);
        model.addAttribute("account", account);
        return "dashboard";
    }

    @GetMapping("/acc/details")
    public String getAccountDetails(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findByUsername(username);
        model.addAttribute("account", account);
        return "account-details"; // Thymeleaf/JSP page name
    }

    @GetMapping("/register")
    public String showregistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerAccount(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String accountHolder,
            @RequestParam String aadhaarNumber,
            @RequestParam String panNumber,
            @RequestParam String mobileNumber,
            @RequestParam String email,
            @RequestParam String address,
            @RequestParam String accountType,
            @RequestParam String location,
            Model model) {
        try {

            // Clean the mobile number before saving (remove +91 if present)
            String cleanedMobile = mobileNumber.startsWith("+91") ? mobileNumber.substring(3) : mobileNumber;

            // Create account first
            Account account = accountService.registerAccount(username, password, accountHolder, aadhaarNumber,
                    panNumber, cleanedMobile, email, address, accountType, location);

            // Pass accountId to OTP page/modal
            model.addAttribute("accountId", account.getId());
            model.addAttribute("email", account.getEmail());
            model.addAttribute("mobileNumber", account.getMobileNumber());

            return "verify-otp"; // Go to OTP verification page/modal

        } catch (RuntimeException ex) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy"; // This should match privacy-policy.html in templates
    }

    @GetMapping("/termsofservice")
    public String termsofservice() {
        return "termsofservice"; // This should match termsofservice.html in templates
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findByUsername(username);

        try {
            accountService.deposit(account, amount);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("account", account);
            // You might want to return to the dashboard with the error message
            return "dashboard";
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findByUsername(username);
        try {
            accountService.withdraw(account, amount);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("account", account);
            return "dashboard";
        }

        return "redirect:/dashboard";

    }

    // Controller for the Access Denied page
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied"; // Return the name of our new HTML file
    }

    @GetMapping("/transactions")
    public String transactionsHistory(Model model, @RequestParam(defaultValue = "0") int page) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findByUsername(username);
        Page<Transaction> transactionPage = accountService.getTransactions(account, page, 10); // Show 10 per page

        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionPage.getTotalPages());

        return "transactions";
    }

    // Transaction Statement Download PDF
    @GetMapping("/transactions/download")
    public void downloadTransactionPDF(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpServletResponse response) throws IOException {

        // Get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findByUsername(username);

        // Mask account number
        String accountNumber = String.valueOf(account.getId());
        String maskedAccNo = "XXXX" + accountNumber.substring(accountNumber.length() - 4);

        // Current date
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        // Filename with date
        String fileName = "acc_" + maskedAccNo + "_Account-Statement_" + today + ".pdf";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        response.setContentType("application/pdf");

        List<Transaction> transactions;

        if (fromDate != null && toDate != null) {
            // Date Range Mode
            transactions = accountService.getTransactionsByDateRange(
                    account, fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay());
        } else {
            // Full Statement Mode
            transactions = accountService.getTransactions(account);
        }

        // PDF Creation
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Main Title
        Font bankFont = new Font(Font.HELVETICA, 22, Font.BOLD, Color.BLUE);
        Paragraph bankTitle = new Paragraph("FAKE BANK OF INDIA", bankFont);
        bankTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(bankTitle);

        // Sub-Title
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.DARK_GRAY);
        Paragraph subTitle = new Paragraph("Transaction Statement", titleFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subTitle);
        document.add(new Paragraph(" ")); // Add space
        // Account Details
        document.add(new Paragraph("Account Holder: " + account.getAccountHolder()));
        document.add(new Paragraph("Account Type: " + account.getAccountType()));
        document.add(new Paragraph("Account Number: " + maskedAccNo));

        document.add(new Paragraph("Customer's Address: " + account.getAddress()));
        document.add(new Paragraph("Email: " + account.getEmail()));
        document.add(new Paragraph("Mobile Number: " + account.getMobileNumber()));
        document.add(new Paragraph("Branch Name: " + account.getLocation()));

        document.add(new Paragraph("Account Balance: Rs." + account.getBalance() + " /- "));
        document.add(new Paragraph(" ")); // Add space

        if (fromDate != null && toDate != null) {
            document.add(new Paragraph("Date Range: " + fromDate + " to " + toDate));
        } else {
            document.add(new Paragraph("Full Transaction History"));
        }

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        String[] headers = { "Transaction ID", "Type", "Amount", "Date" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h));
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
        }

        for (Transaction tx : transactions) {
            table.addCell(String.valueOf(tx.getId()));
            table.addCell(tx.getType());
            table.addCell(String.valueOf(tx.getAmount()));
            table.addCell(String.valueOf(tx.getTimestamp()));
        }

        document.add(table);
        document.close();
    }

    @GetMapping("/transactions/email")
    public String emailTransactionPDF(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            RedirectAttributes redirectAttributes) {

        try {
            // Get current user
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account account = accountService.findByUsername(username);

            // Mask account number & setup filename
            String accountNumber = String.valueOf(account.getId());
            String maskedAccNo = "XXXX" + accountNumber.substring(accountNumber.length() - 4);
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String fileName = "acc_" + maskedAccNo + "_Account-Statement_" + today + ".pdf";

            // Fetch Transactions
            List<Transaction> transactions;
            if (fromDate != null && toDate != null) {
                transactions = accountService.getTransactionsByDateRange(
                        account, fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay());
            } else {
                transactions = accountService.getTransactions(account);
            }

            // PDF Creation (In Memory via ByteArrayOutputStream)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Main Title
            Font bankFont = new Font(Font.HELVETICA, 22, Font.BOLD, Color.BLUE);
            Paragraph bankTitle = new Paragraph("FAKE BANK OF INDIA", bankFont);
            bankTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(bankTitle);

            // Sub-Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.DARK_GRAY);
            Paragraph subTitle = new Paragraph("Transaction Statement", titleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);
            document.add(new Paragraph(" "));

            // Account Details
            document.add(new Paragraph("Account Holder: " + account.getAccountHolder()));
            document.add(new Paragraph("Account Type: " + account.getAccountType()));
            document.add(new Paragraph("Account Number: " + maskedAccNo));
            document.add(new Paragraph("Customer's Address: " + account.getAddress()));
            document.add(new Paragraph("Email: " + account.getEmail()));
            document.add(new Paragraph("Mobile Number: " + account.getMobileNumber()));
            document.add(new Paragraph("Branch Name: " + account.getLocation()));
            document.add(new Paragraph("Account Balance: Rs." + account.getBalance() + " /- "));
            document.add(new Paragraph(" "));

            if (fromDate != null && toDate != null) {
                document.add(new Paragraph("Date Range: " + fromDate + " to " + toDate));
            } else {
                document.add(new Paragraph("Full Transaction History"));
            }
            document.add(new Paragraph(" "));

            // Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            String[] headers = { "Transaction ID", "Type", "Amount", "Date" };
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (Transaction tx : transactions) {
                table.addCell(String.valueOf(tx.getId()));
                table.addCell(tx.getType());
                table.addCell(String.valueOf(tx.getAmount()));
                table.addCell(String.valueOf(tx.getTimestamp()));
            }

            document.add(table);
            document.close();

            // Send the email using the byte array
            emailService.sendStatementEmail(account.getEmail(), account.getAccountHolder(), baos.toByteArray(),
                    fileName);

            // Add a success message to show on the UI
            redirectAttributes.addFlashAttribute("successMessage",
                    "Statement successfully sent to your registered email!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send statement. Please try again later.");
        }

        // Redirect back to the transactions page
        return "redirect:/transactions";
    }

    @PostMapping("/transfer")
    public String transferamount(@RequestParam String toUsername, @RequestParam BigDecimal amount, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account fromAccount = accountService.findByUsername(username);

        // beneficiaries
        if (fromAccount != null) {
            Beneficiary byNick = beneficiaryService.findByNickname(fromAccount, toUsername);
            if (byNick != null && byNick.getActualUsername() != null && !byNick.getActualUsername().isBlank()) {
                toUsername = byNick.getActualUsername(); // map nickname -> real username
            }
        }

        Account toAccount = accountService.findByUsername(toUsername);

        // (rest of your checks remain unchanged)
        if (fromAccount == null) {
            model.addAttribute("error", "Your account does not exist");
            return "dashboard";
        }

        if (toAccount == null) {
            model.addAttribute("error", "Recipient account does not exist");
            model.addAttribute("account", fromAccount);
            return "dashboard";
        }
        if (fromAccount.getUsername().equals(toUsername)) {
            model.addAttribute("error", "Cannot transfer to the same account");
            model.addAttribute("account", fromAccount);
            return "dashboard";
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Transfer amount must be greater than zero");
            model.addAttribute("account", fromAccount);
            return "dashboard";
        }
        try {
            accountService.transfer(fromAccount, toAccount, amount);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("account", fromAccount);
            return "dashboard";
        }
        return "redirect:/dashboard";
    }

}
