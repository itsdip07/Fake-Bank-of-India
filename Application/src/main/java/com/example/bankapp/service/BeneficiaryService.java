package com.example.bankapp.service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Beneficiary;
import com.example.bankapp.repository.BeneficiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BeneficiaryService {

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    public List<Beneficiary> listBeneficiaries(Account owner) {
        return beneficiaryRepository.findByOwner(owner);
    }

    public Beneficiary saveBeneficiary(Beneficiary b) {
        return beneficiaryRepository.save(b);
    }

    public Beneficiary findByNickname(Account owner, String nickname) {
        return beneficiaryRepository.findByOwnerAndNicknameIgnoreCase(owner, nickname).orElse(null);
    }

    public Beneficiary findByActualUsername(Account owner, String actualUsername) {
        return beneficiaryRepository.findByOwnerAndActualUsername(owner, actualUsername).orElse(null);
    }

    public void deleteById(Long id) { beneficiaryRepository.deleteById(id); }
}
