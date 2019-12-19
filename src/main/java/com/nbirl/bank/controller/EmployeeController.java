package com.nbirl.bank.controller;

import com.nbirl.bank.model.Account;
import com.nbirl.bank.model.User;
import com.nbirl.bank.repository.UserRepository;
import com.nbirl.bank.service.auth.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;


@Controller
public class EmployeeController {

    @Autowired
    private UserRepository userRepository;
    Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @GetMapping("/search-customer")
    public String SearchAccount(Model model){
        model.addAttribute("account",new Account());
        return "search-account";
    }

    @PostMapping("/search-customer")
    public String FindAccount(@ModelAttribute Account account, Model model,
                                BindingResult bindingResult){
        User user = userRepository.findByAccount_AccountNumber(account.getAccountNumber());
        if(user==null) {
            bindingResult.rejectValue("accountNumber", "user.account.accountNumber.Error",
                        "Account Number not found..!!");
            return "search-account";
            }
        model.addAttribute("user", user);
        return "account-detail";
        }
    @PostMapping("/deposit")
    public String DepositAmount(@ModelAttribute Account account, Principal principal,
                                Model model, BindingResult bindingResult){
        User user = userRepository.findByAccount_AccountNumber(account.getAccountNumber());
        if(user==null)
            bindingResult.rejectValue("accountNumber", "user.account.accountNumber.Error",
                    "Account Number not found..!!");

        if(!account.getAccountNumber().equals(account.getBic()))
            bindingResult.rejectValue("bic", "user.account.bic.Error",
                    "Account Numbers  doesn't matching..!!");
        if(account.getTransfer()<0)
            bindingResult.rejectValue("transfer", "user.account.bic.Error",
                    "Enter a amount greater than 0");
        if(bindingResult.hasErrors())
            return "index";
        logger.info( principal.getName() + " is trying to deposited $" + account.getTransfer() + " in the account"
                    + account.getAccountNumber());
        model.addAttribute("user", user);
        model.addAttribute("account", account);
        return "deposit";
    }
    @PostMapping("/confirm-deposit")
    public String DepositConfirm(@ModelAttribute Account account, Model model, Principal principal){
        User user = userRepository.findByAccount_AccountNumber(account.getBic());
        user.getAccount().setBalance(user.getAccount().getBalance()+account.getTransfer());
        logger.info( principal.getName() + " has deposited the amount to the user");
        userRepository.save(user);
        model.addAttribute("user", user);
        return "account-detail";
    }


    }
