package com.example.bankapp.model;

import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue; // It is commented out because we are generating ID manually
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Account implements UserDetails {

    @Id
    private Long id;
    private String username;
    private String password;
    private String accountHolder;
    private String aadhaarNumber;
    private String panNumber;
    private String mobileNumber;
    private String email;
    private String address;
    private String accountType;
    private String location;

    // OTP SECTION
    private String otp;
    private LocalDateTime otpGeneratedTime;
    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "frozen", nullable = false, columnDefinition = "boolean default false")
    private boolean frozen = false;

    private BigDecimal balance;

    @OneToMany(mappedBy = "account")
    private List<Transaction> transactions;

    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    public Account() {
    }

    public Account(String username, String password, BigDecimal balance, List<Transaction> transactions,
            Collection<? extends GrantedAuthority> authorities, String accountHolder,
            String aadhaarNumber, String panNumber, String mobileNumber,
            String email, String address, String accountType, String location) {
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.transactions = transactions;
        this.authorities = authorities;
        this.accountHolder = accountHolder;
        this.aadhaarNumber = aadhaarNumber;
        this.panNumber = panNumber;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.address = address;
        this.accountType = accountType;
        this.location = location;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    // Getter for otp
    public String getOtp() {
        return otp;
    }

    // Setter for otp
    public void setOtp(String otp) {
        this.otp = otp;
    }

    // Getter for otpGeneratedTime
    public LocalDateTime getOtpGeneratedTime() {
        return otpGeneratedTime;
    }

    // Setter for otpGeneratedTime
    public void setOtpGeneratedTime(LocalDateTime otpGeneratedTime) {
        this.otpGeneratedTime = otpGeneratedTime;
    }

    // Getter for verified
    public boolean isVerified() {
        return verified;
    }

    // Setter for verified
    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    //Getter and setter for the 'frozen' field
    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    //MISSING UserDetails METHODS
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.frozen;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Use the 'verified' field to determine if the account is enabled for login
        return this.verified && !this.frozen;
    }

}
