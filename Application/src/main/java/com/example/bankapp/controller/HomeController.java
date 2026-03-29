package com.example.bankapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String landingPage() {
        return "index"; // loads index.html
    }

    @GetMapping("/support_page.html")
    public String supportPage() {
        return "support_page";
    }

    @GetMapping("/debit.html")
    public String debitPage() {
        return "debit";
    }

    @GetMapping("/current_acc.html")
    public String currentAccountPage() {
        return "current_acc"; 
    }

    @GetMapping("/savings_acc.html")
    public String savingsAccountPage() {
        return "savings_acc"; 
    }

    @GetMapping("/demat.html")
    public String dematPage() {
        return "demat"; 
    }

    @GetMapping("/fixed_deposit.html")
    public String fixedDepositPage() {
        return "fixed_deposit"; 
    }

    @GetMapping("/credit_cards.html")
    public String creditCardsPage() {
        return "credit_cards"; 
    }

    @GetMapping("/personal_loans.html")
    public String personalLoansPage() {
        return "personal_loans"; 
    }

    @GetMapping("/home_loans.html")
    public String homeLoansPage() {
        return "home_loans"; 
    }

    @GetMapping("/ex_products_page.html")
    public String productsPage() {
        return "ex_products_page"; // Make sure your file is named ex_products_page.html
    }

    @GetMapping("/ai_banking.html")
    public String aiBankingPage() {
        // This looks for src/main/resources/templates/ai_banking.html
        return "ai_banking"; // Note: return the exact file name WITHOUT the .html extension
    }

    @GetMapping("/investments.html")
    public String investmentsPage() {
        // This looks for src/main/resources/templates/investments.html
        return "investments"; 
    }

    @GetMapping("/groww.html")
    public String stocksMutualFundsPage() {
        return "groww"; 
    }

    @GetMapping("/vehicle_loans.html")
    public String vehicleLoansPage() {
        // This looks for src/main/resources/templates/vehicle_loans.html
        return "vehicle_loans"; 
    }
}
