package com.example.bankapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_users") // Use a separate table for admins
public class Admin {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "username")
    private String username;

    @Column(name = "admin_name")
    private String admin_name;

    public Admin() {
    }

    public Admin(Long accountId, String username, String admin_name) {
        this.accountId = accountId;
        this.username = username;
        this.admin_name = admin_name;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getadmin_name() {
        return admin_name;
    }

    public void setadmin_name(String admin_name) {
        this.admin_name = admin_name;
    }
}