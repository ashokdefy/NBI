package com.nbirl.bank.controller;

import com.nbirl.bank.athorizer.RecaptchaService;
import com.nbirl.bank.model.Account;
import com.nbirl.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;
    @Autowired
    private RecaptchaService recaptchaService;

    @GetMapping("/login")
    public String login(HttpSession httpSession, Model model, String error){
        httpSession.invalidate();
        if (error != null)
            model.addAttribute("error", "Your username and password is invalid.");
        return "login";
    }

    @PostMapping("/login")
    public String processLogin() { return "redirect:/index";    }

    @GetMapping({"/","","/index"})
    public String myHome(HttpServletRequest request, Model model,Principal principal) {

        if(request.isUserInRole("CUSTOMER")) {
            Account account  = userService.findByUsername(principal.getName()).getAccount();
                model.addAttribute("account", account);
            return "user-index";
        }
        if(request.isUserInRole("EMPLOYEE")) {
            model.addAttribute("account", new Account());
            return "index";
        }
        return "index";
    }
}
