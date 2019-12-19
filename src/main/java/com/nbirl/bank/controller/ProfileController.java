package com.nbirl.bank.controller;

import com.nbirl.bank.model.User;
import com.nbirl.bank.repository.UserRepository;
import com.nbirl.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@RequestMapping("/profile")
@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping({"/",""})
    public String userProfile(Principal principal, Model model){
        User user = userService.findByUsername(principal.getName());
        if (user != null) {
            model.addAttribute("user", user);
        }else { return "error/404";  }
        return "profile";
    }
    @PostMapping({"/",""})
    public String processLogin() {
        return "profile";
    }

        @GetMapping("/update")
    public String userEdit(Principal principal, Model model){
        User user = userService.findByUsername(principal.getName());
        if (user == null)
            return "error/404";
        model.addAttribute("user", user);
        return "edit-profile";
    }
    @PostMapping({"/update"})
    public String editProfile(@ModelAttribute("user")
        User user, Principal principal, BindingResult bindingResult) {
        User userUpdate = userService.findByUsername(principal.getName());
        editProcess(user, userUpdate, bindingResult);
        if (bindingResult.hasErrors()) {
            return "edit-profile";
        }
        userRepository.save(userUpdate);
        return "redirect:/profile";
    }

    @GetMapping("/password")
    public String userPassword(Model model){
        User user = new User();
        model.addAttribute("user", user);
        return "/verification/password-change";
    }
    @PostMapping("/password")
    public String setPassword(@ModelAttribute("user") User user,
                              Principal principal, BindingResult bindingResult){
            User userCheck = userService.findByUsername(principal.getName());
            if(!(bCryptPasswordEncoder.matches(user.getFirstName(), userCheck.getPassword())))
                bindingResult.rejectValue("firstName", "user.password.change", "Old Password doesn't match.");
            if(!user.getPassword().equals(user.getPasswordConfirm()))
                bindingResult.rejectValue("passwordConfirm", "user.passwordConfirm.change", "New Passwords doesn't match.");
        //Password must have at least 8 characters and max 32
            if (user.getPassword().length() < 8)
            bindingResult.rejectValue("password", "user.password.error","Password must have at least 8 characters.");
            else if (user.getPassword().length() > 32)
                bindingResult.rejectValue("password","user.password.error", "Password can't have more than 32 characters.");
            else if (!(user.getPassword().matches("(?=.*[0-9]).*")
                    && user.getPassword().matches("(?=.*[a-z]).*")
                    && user.getPassword().matches("(?=.*[A-Z]).*")
                    && user.getPassword().matches("(?=.*[~!@#$%^&*()_-]).*"))){
                bindingResult.rejectValue("password","user.password.error", "Password must have atleast one Uppercase, Lowercase, Numeric and a Symbol.");
                userCheck.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
           }
        if(bindingResult.hasErrors())
                return "/verification/password-change";
            userRepository.save(userCheck);
        return "redirect:/profile";
    }

    private void editProcess(User user, User userUpdate, BindingResult bindingResult) {
        userUpdate.setFirstName(user.getFirstName());
        userUpdate.setLastName(user.getLastName());
        userUpdate.setEmail(user.getEmail().toLowerCase());
        userUpdate.setMobileNumber(user.getMobileNumber());
        userUpdate.setPasswordConfirm("dummy");
        userUpdate.setCountrycode("dummy");
        if (!(user.getMobileNumber() != null &&
                user.getMobileNumber().matches("^[+][0-9]+") &&
                (user.getMobileNumber().length() > 11) && (user.getMobileNumber().length() < 17))) {
            bindingResult.rejectValue("mobileNumber", "user.mobile.error", "Enter a correct mobile number with country code");
        }
        if (userService.findByEmail(user.getEmail()) != null && userService.findByEmail(user.getEmail()).equals(user.getEmail())){
            bindingResult.rejectValue("email", "user.email.error","This E-mail is already in use.");
        }
    }

}
