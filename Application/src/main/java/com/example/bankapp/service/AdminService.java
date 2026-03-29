package com.example.bankapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;

@Service
public class AdminService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }
    
    //Method to find all transactions
    public List<Transaction> findAllTransactions() {
        return transactionRepository.findAll();
    }

    public Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));
    }

    public void freezeAccount(Long id) {
        Account account = findAccountById(id);
        account.setFrozen(true);
        accountRepository.save(account);
    }
    
    public void unfreezeAccount(Long id) {
        Account account = findAccountById(id);
        account.setFrozen(false);
        accountRepository.save(account);
    }
}