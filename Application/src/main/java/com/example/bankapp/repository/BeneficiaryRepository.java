package com.example.bankapp.repository;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    Optional<Beneficiary> findByOwnerAndNicknameIgnoreCase(Account owner, String nickname);
    Optional<Beneficiary> findByOwnerAndActualUsername(Account owner, String actualUsername);
    List<Beneficiary> findByOwner(Account owner);
}
