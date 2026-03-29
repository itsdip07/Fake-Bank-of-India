package com.example.bankapp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
//import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bankapp.OTP.otpUtil;
import com.example.bankapp.model.Account;
import com.example.bankapp.model.Admin;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.AdminRepository;
import com.example.bankapp.repository.TransactionRepository;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private otpUtil otpUtil;

    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public Account registerAccount(String username, String password, String accountHolder,
            String aadhaarNumber, String panNumber, String mobileNumber,
            String email, String address, String accountType, String location) {
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Generate unique 8-9 digit account ID
        Long accountId = null;
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            long candidateId = (long) (ThreadLocalRandom.current().nextDouble() * 900000000L) + 10000000L; // 8-9 digits
            if (accountRepository.findById(candidateId).isEmpty()) {
                accountId = candidateId;
                break;
            }
        }
        if (accountId == null) {
            throw new IllegalStateException("Could not generate unique account ID");
        }

        Account account = new Account();
        account.setId(accountId);
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setAccountHolder(accountHolder);
        account.setAadhaarNumber(aadhaarNumber);
        account.setPanNumber(panNumber);
        account.setMobileNumber(mobileNumber);
        account.setEmail(email);
        account.setAddress(address);
        account.setAccountType(accountType);
        account.setLocation(location);
        account.setBalance(BigDecimal.ZERO); // Initialize balance to zero
        account.setVerified(false); // Initially not verified

        // Generate OTP and set time BEFORE saving the account
        String otp = otpUtil.generateOtp();
        account.setOtp(otp);
        account.setOtpGeneratedTime(LocalDateTime.now());

        // Save the account ONLY ONCE after all properties are set
        Account savedAccount = accountRepository.save(account);

        // Send OTP via SmsService
        smsService.sendOtpSms(mobileNumber, otp);

        // Send OTP via EmailService (if enabled)
        emailService.sendOtpEmail(email, otp);

        // Return saved account
        return savedAccount;
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public void deposit(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        transactionService.createTransaction(amount, "DEPOSIT", account);

        emailService.sendCreditEmail(account.getEmail(), account.getAccountHolder(), amount, account.getBalance(), "Account Deposit");
    }

    public void withdraw(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        transactionService.createTransaction(amount, "WITHDRAWAL", account);
        emailService.sendDebitEmail(account.getEmail(), account.getAccountHolder(), amount, account.getBalance(), "Account Withdrawal");

    }

    public List<Transaction> getTransactions(Account account) {
        return transactionRepository.findByAccount(account);
    }

    public Page<Transaction> getTransactions(Account account, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return transactionRepository.findByAccount(account, pageRequest);
    }

    public List<Transaction> getTransactionsByDateRange(Account account, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByAccountAndTimestampBetween(account, from, to);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("Attempting to load user with username: '" + username + "'");

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Check if the user is an administrator
        Optional<Admin> admin = adminRepository.findByAccountId(account.getId());
        if (admin.isPresent()) {
            account.setAuthorities(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        } else {
            account.setAuthorities(Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        }
        return account;
    }

    public Collection<? extends GrantedAuthority> authorities() {
        return Arrays.asList(new SimpleGrantedAuthority("USER"));
    }

    public void transfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // New Debit Way after creating the TransactionService
        transactionService.createTransaction(amount, "Sent_to " + toAccount.getUsername(), fromAccount);
        // Credit transaction for the receiver
        transactionService.createTransaction(amount, "Recieved_from " + fromAccount.getUsername(), toAccount);

        emailService.sendDebitEmail(fromAccount.getEmail(), fromAccount.getAccountHolder(), amount, fromAccount.getBalance(), "Fund Transfer to " + toAccount.getUsername());
        emailService.sendCreditEmail(toAccount.getEmail(), toAccount.getAccountHolder(), amount, toAccount.getBalance(), "Fund Transfer from " + fromAccount.getUsername());
    }

}
