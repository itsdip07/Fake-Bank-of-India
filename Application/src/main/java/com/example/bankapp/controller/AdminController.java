package com.example.bankapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.service.AdminService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // All methods in this controller require the ADMIN role
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/accounts")
    public String manageAccounts(Model model) {
        List<Account> accounts = adminService.findAllAccounts();
        model.addAttribute("accounts", accounts);
        return "admin-manage-accounts";
    }

    // NEW: Controller method to view all transactions
    @GetMapping("/transactions")
    public String viewTransactions(Model model) {
        List<Transaction> transactions = adminService.findAllTransactions();
        model.addAttribute("transactions", transactions);
        return "admin-transactions";
    }
    
    @PostMapping("/freeze-account")
    public String freezeAccount(@RequestParam Long accountId, RedirectAttributes redirectAttributes) {
        try {
            adminService.freezeAccount(accountId);
            redirectAttributes.addFlashAttribute("success", "Account frozen successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/accounts";
    }
    
    @PostMapping("/unfreeze-account")
    public String unfreezeAccount(@RequestParam Long accountId, RedirectAttributes redirectAttributes) {
        try {
            adminService.unfreezeAccount(accountId);
            redirectAttributes.addFlashAttribute("success", "Account unfrozen successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/accounts";
    }
}