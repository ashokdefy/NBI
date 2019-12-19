package com.nbirl.bank.controller;

import com.nbirl.bank.model.Account;
import com.nbirl.bank.model.User;
import com.nbirl.bank.repository.AccountRepository;
import com.nbirl.bank.repository.UserRepository;
import com.nbirl.bank.service.UserService;
import com.nbirl.bank.validator.TransferValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class TransferController {

    private final UserService userService;
    private final TransferValidator transferValidator;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public TransferController(UserService userService, TransferValidator transferValidator) {
        this.userService = userService;
        this.transferValidator = transferValidator;
    }

    @GetMapping("/money-transfer")
    public String searchAccount(Model model, Principal principal) {
        model.addAttribute("account", new Account());
        model.addAttribute("balance", userService.findByUsername(principal.getName()).getAccount().getBalance());

        return "money-transfer";
    }

    @PostMapping("/money-transfer")
    public String transferAccount(@ModelAttribute("account") Account account,
                                  @RequestParam(value = "password") String password,
                                  Model model, Principal principal, BindingResult bindingResult) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("balance", user.getAccount().getBalance());
        transferValidator.validate(account, bindingResult);
        if(user.getAccount().getBalance()<account.getTransfer())
            bindingResult.rejectValue("transfer", "transfer.error",
                    "Your don't have enough credit to transfer!");
        if(!bCryptPasswordEncoder.matches(password, user.getPassword()))
            bindingResult.rejectValue("balance", "user.password.change", "Enter the correct password");
        if(bindingResult.hasErrors())
           return "money-transfer";
        user.getAccount().setBalance(user.getAccount().getBalance() - account.getTransfer());
        accountRepository.save(user.getAccount());
        Account transferAccount =  userService.findByAccountNumber(account.getAccountNumber());
        transferAccount.setBalance(transferAccount.getBalance()+account.getTransfer());
        accountRepository.save(transferAccount);
        return "redirect:/";
    }
}
