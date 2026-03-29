package com.example.bankapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankapp.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    Optional<Admin> findByAccountId(Long accountId);
}