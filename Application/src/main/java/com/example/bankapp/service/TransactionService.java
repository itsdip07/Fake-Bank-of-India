package com.example.bankapp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.bankapp.dto.TransactionDTO;
import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(BigDecimal amount, String type, Account account) {

        // Generate unique transaction ID
        Long transactionId = null;
        int maxAttempts = 10;

        for (int i = 0; i < maxAttempts; i++) {
            long candidateId = (long) (ThreadLocalRandom.current().nextDouble() * 900000000000L) + 1000000000L; // 10-12 digits
            if (transactionRepository.findById(candidateId).isEmpty()) {
                transactionId = candidateId;
                break;
            }
        }

        if (transactionId == null) {
            throw new IllegalStateException("Could not generate unique transaction ID");
        }

        // Create Transaction
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setAccount(account);

        return transactionRepository.save(transaction);
    }

    public List<TransactionDTO> getLastFiveTransactions(Account account) {
    Pageable topFive = PageRequest.of(0, 5, Sort.by("timestamp").descending());
    Page<Transaction> page = transactionRepository.findByAccount(account, topFive);

    return page.getContent().stream()
            .map(t -> new TransactionDTO(
                    t.getId(),
                    t.getAmount(),
                    t.getType(),
                    t.getTimestamp()
            ))
            .toList();
}

}
