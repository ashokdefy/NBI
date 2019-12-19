package com.nbirl.bank.controller;

import com.nbirl.bank.athorizer.RecaptchaService;
import com.nbirl.bank.model.User;
import com.nbirl.bank.model.VerificationToken;
import com.nbirl.bank.repository.UserRepository;
import com.nbirl.bank.repository.VerificationTokenRepository;
import com.nbirl.bank.service.UserService;
import com.nbirl.bank.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class RegisterController {
        private final UserService userService;
        private final UserValidator userValidator;
        @Autowired
        private BCryptPasswordEncoder passwordEncoder;

        @Autowired
        private VerificationTokenRepository verificationTokenRepository;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private JavaMailSender javaMailSender;
        @Autowired
        private RecaptchaService recaptchaService;

        @Autowired
        public RegisterController(UserService userService, UserValidator userValidator) {
            this.userService = userService;
            this.userValidator = userValidator;
        }

        @GetMapping("/register")
        public String registration(Model model, HttpSession httpSession) {
            httpSession.invalidate();
            model.addAttribute("userForm", new User());
            return "register";
        }

        @PostMapping("/register")
        public String registration(@ModelAttribute("userForm") User userForm, BindingResult bindingResult,
                                   @RequestParam(name="g-recaptcha-response") String recaptchaResponse, HttpServletRequest request) {
            userValidator.validate(userForm, bindingResult);

            String ip = request.getRemoteAddr();
            String captchaVerifyMessage =
                    recaptchaService.verifyRecaptcha(ip, recaptchaResponse);
            if(!captchaVerifyMessage.equals(""))
                bindingResult.rejectValue("reCaptcha", "user.reCaptcha.error","Recaptcha requires verification. ");

            if (bindingResult.hasErrors())
                return "register";
            userService.save(userForm);
            VerificationToken verificationToken = new VerificationToken(userForm);

            verificationTokenRepository.save(verificationToken);

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(userForm.getEmail());
            mailMessage.setSubject("Complete Registration!");
            mailMessage.setFrom("noreply@nbirl.com");
            mailMessage.setText("To confirm your account, please click here : "
                    +"http://localhost:443/confirm-account?token="+verificationToken.getToken());
            javaMailSender.send(mailMessage);

            return "/verification/redirect-mail";
        }
    @RequestMapping(value="/confirm-account")
    public String confirmUserAccount(@RequestParam( value = "token" , required = false)String token )
    {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if(verificationToken == null)
           return "error/verification-error";

        User user = userRepository.findByEmail(verificationToken.getUser().getEmail());
        user.setEnabled(true);
        verificationTokenRepository.delete(verificationToken);
        userRepository.save(user);
        return "/verification/account-verified";
    }
    @GetMapping(value="/forgot-password")
    public String ForgotPassword(Model model){
        model.addAttribute("user", new User());
        return "forgot-password";
    }

    @PostMapping(value="/forgot-password")
    public String ResetPassword(@ModelAttribute User user,  Model model, BindingResult bindingResult){
        User UserCheck = userRepository.findByUsername(user.getUsername());
        if(UserCheck==null)
            bindingResult.rejectValue("username", "user.username.error","Username not found");
        else if(!UserCheck.getEmail().equals(user.getEmail()))
                bindingResult.rejectValue("email", "user.email.error","User Email doesn't match with username");
        model.addAttribute(user);
        if(bindingResult.hasErrors())
            return "forgot-password";
        Random random = new Random();
        String otp = random.nextInt(99999999)+"";
        System.out.println(otp);
        VerificationToken verificationToken = new VerificationToken(UserCheck, otp);
        verificationTokenRepository.save(verificationToken);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(UserCheck.getEmail());
        mailMessage.setSubject("Reset Password!");
        mailMessage.setFrom("noreply@nbirl.com");
        mailMessage.setText("OTP to reset your National Bank of Ireland Account is : " + otp);
        javaMailSender.send(mailMessage);

        return "reset-password";
    }
    @GetMapping(value="/reset-password")
    public String ReturnLogin(){
            return "redirect:/";
    }
    @PostMapping(value="/reset-password")
    public String ChangePassword(@ModelAttribute User user, Model model, BindingResult errors){
       if (user.getPassword().length() < 8)
            errors.rejectValue("password", "user.password.error","Password must have at least 8 characters.");
       else if (user.getPassword().length() > 32)
            errors.rejectValue("password","user.password.error", "Password can't have more than 32 characters.");
       else if (!(user.getPassword().matches("(?=.*[0-9]).*")
               && user.getPassword().matches("(?=.*[a-z]).*")
               && user.getPassword().matches("(?=.*[A-Z]).*")
               && user.getPassword().matches("(?=.*[~!@#$%^&*()_-]).*"))){
           errors.rejectValue("password","user.password.error", "Password must have atleast one Uppercase, Lowercase, Numeric and a Symbol.");
       }
        if (!user.getPasswordConfirm().equals(user.getPassword()))
            errors.rejectValue("password","user.password.error", "Passwords Doesn't  match");
        VerificationToken token = verificationTokenRepository.findByToken(user.getLastName());
        if(token==null)
            errors.rejectValue("lastName","user.lastName.error", "Incorrect OTP, Check your mail properly");
        else if(!token.getUser().getUsername().equals(user.getUsername()))
            errors.rejectValue("lastName","user.lastName.error", "Invalid user");
        System.out.println(token + "\n" + user);
        if(errors.hasErrors()) {
            model.addAttribute(user);
            return "/reset-password";
        }
        token.getUser().setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(token.getUser());
        return "/login";
    }
}
