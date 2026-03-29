package com.example.bankapp.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.bankapp.dto.ChatbotResponse;
import com.example.bankapp.dto.TransactionDTO;
import com.example.bankapp.model.Account;
import com.example.bankapp.model.Beneficiary;
import com.example.bankapp.service.AccountService;
import com.example.bankapp.service.BeneficiaryService;
import com.example.bankapp.service.TransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BeneficiaryService beneficiaryService;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String pythonServiceUrl = "http://localhost:5000/ai/query";

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> postChatbotQuery(@RequestBody Map<String, String> body) {
        try {
            String userQuery = body.getOrDefault("query", "");
            if (userQuery == null || userQuery.isBlank()) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", false);
                resp.put("message", "Empty query");
                return ResponseEntity.badRequest().body(resp);
            }

            //Call Python AI service
            String requestBodyJson = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pythonServiceUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "AI service unavailable");
                return ResponseEntity.status(500).body(err);
            }

            //Map Python JSON into ChatbotResponse DTO
            ChatbotResponse aiResp = objectMapper.readValue(response.body(), ChatbotResponse.class);

            //Debug print
            System.out.println("[AI RAW RESPONSE] " + response.body());

            //Get current logged-in user's account
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account userAccount = accountService.findByUsername(username);

            //Route by intent
            String intent = aiResp.getIntent() == null ? "unknown" : aiResp.getIntent();
            Map<String, Object> out = new HashMap<>();

            switch (intent) {
                case "get_balance":
                    out.put("success", true);
                    out.put("intent", intent);
                    out.put("message", aiResp.getMessage());
                    out.put("balance", userAccount != null ? userAccount.getBalance() : null);
                    return ResponseEntity.ok(out);

                case "account_details":
                    out.put("success", true);
                    out.put("intent", intent);
                    out.put("message", aiResp.getMessage());
                    if (userAccount != null) {
                        Map<String, Object> details = new HashMap<>();
                        details.put("id", userAccount.getId());
                        details.put("accountHolder", userAccount.getAccountHolder());
                        details.put("balance", userAccount.getBalance());
                        details.put("accountType", userAccount.getAccountType());
                        details.put("mobileNumber", userAccount.getMobileNumber());
                        details.put("email", userAccount.getEmail());
                        out.put("details", details);
                    }
                    return ResponseEntity.ok(out);

                case "transaction_history":
                    //Expect transactionService.getLastFiveTransactions(Account) -> List<TransactionDTO>
                    List<TransactionDTO> last5 = transactionService.getLastFiveTransactions(userAccount);

                    out.put("success", true);
                    out.put("intent", intent);
                    out.put("message", aiResp.getMessage());
                    out.put("transactions", last5);
                    return ResponseEntity.ok(out);

                case "download_statement":
                    //Generate a direct download link for the logged-in user (front-end downloads from this path)
                    String downloadUrl = "/transactions/download";

                    out.put("success", true);
                    out.put("intent", intent);
                    out.put("message", "Preparing your statement…");
                    out.put("downloadUrl", downloadUrl);
                    return ResponseEntity.ok(out);

                case "initiate_transfer":
                    // AI asks for missing info or returned a pending id
                    out.put("success", true);
                    out.put("intent", intent);
                    out.put("message", aiResp.getMessage());
                    out.put("params", aiResp.getParams());
                    return ResponseEntity.ok(out);

                case "fund_transfer":
                case "transfer_funds": {
                    Map<String, Object> params = aiResp.getParams() == null ? new HashMap<>() : aiResp.getParams();

                    Object amtObj = params.get("amount");
                    Object recObj = params.get("recipient");

                    // Parse amount safely
                    BigDecimal amount = null;
                    if (amtObj != null) {
                        try {
                            amount = new BigDecimal(String.valueOf(amtObj));
                        } catch (Exception ex) {
                            amount = null;
                        }
                    }

                    String spokenName = recObj == null ? null : String.valueOf(recObj).trim();

                    if (amount == null || spokenName == null || spokenName.isEmpty()) {
                        out.put("success", false);
                        out.put("message", "Transfer details missing (amount / recipient).");
                        out.put("params", params);
                        return ResponseEntity.badRequest().body(out);
                    }

                    // Try to resolve spokenName via Beneficiary table (owner = current user)
                    Beneficiary b = null;
                    if (userAccount != null) {
                        b = beneficiaryService.findByNickname(userAccount, spokenName);
                    }

                    String resolvedUsername = null;
                    Account toAccount = null;

                    if (b != null && b.getActualUsername() != null && !b.getActualUsername().isBlank()) {
                        resolvedUsername = b.getActualUsername();
                        toAccount = accountService.findByUsername(resolvedUsername);
                    } else {
                        
                        toAccount = accountService.findByUsername(spokenName);
                        if (toAccount != null) {
                            resolvedUsername = toAccount.getUsername();
                        } else {

                            resolvedUsername = spokenName;
                        }
                    }

                    if (toAccount == null) {
                        // We tried beneficiary and direct username; if still null, fail politely
                        out.put("success", false);
                        out.put("message", "I couldn't find a beneficiary or user matching '" + spokenName + "'.");
                        out.put("params", params);
                        return ResponseEntity.badRequest().body(out);
                    }

                    // Perform transfer
                    try {
                        accountService.transfer(userAccount, toAccount, amount);
                        out.put("success", true);
                        out.put("intent", "transfer_funds");
                        out.put("message", "Successfully transferred " + amount + " to " + spokenName +
                                (b != null ? " (nickname matched to username: " + resolvedUsername + ")" : " (username: " + resolvedUsername + ")"));
                        out.put("params", Map.of("amount", amount, "recipient", resolvedUsername));
                        return ResponseEntity.ok(out);
                    } catch (Exception ex) {
                        out.put("success", false);
                        out.put("message", "Transfer failed: " + ex.getMessage());
                        return ResponseEntity.status(500).body(out);
                    }
                }

                case "greeting":
                case "general_info":
                default:
                    out.put("success", true);
                    out.put("intent", intent);
                    out.put("message", aiResp.getMessage());
                    out.put("params", aiResp.getParams());
                    return ResponseEntity.ok(out);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }
}
