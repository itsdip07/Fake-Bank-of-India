package com.example.bankapp.controller;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Beneficiary;
import com.example.bankapp.service.AccountService;
import com.example.bankapp.service.BeneficiaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/beneficiaries")
public class BeneficiaryController {

    @Autowired
    private BeneficiaryService beneficiaryService;

    @Autowired
    private AccountService accountService;

    private Account getLoggedAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountService.findByUsername(username);
    }

    @GetMapping
    public String list(Model model) {
        Account owner = getLoggedAccount();
        List<Beneficiary> list = beneficiaryService.listBeneficiaries(owner);
        model.addAttribute("beneficiaries", list);
        model.addAttribute("beneficiary", new Beneficiary());
        return "beneficiaries"; // Thymeleaf template name
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Beneficiary b, Model model) {
        Account owner = getLoggedAccount();
        b.setOwner(owner);

        Beneficiary existing = beneficiaryService.findByNickname(owner, b.getNickname());
        if (existing != null) {
            model.addAttribute("error", "A beneficiary with this nickname already exists.");
            model.addAttribute("beneficiaries", beneficiaryService.listBeneficiaries(owner));
            return "beneficiaries";
        }

        beneficiaryService.saveBeneficiary(b);
        return "redirect:/beneficiaries?success=added";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {
        beneficiaryService.deleteById(id);
        return "redirect:/beneficiaries?success=deleted";
    }
}
