package com.nbirl.bank.controller;

import com.nbirl.bank.model.Account;
import com.nbirl.bank.model.Role;
import com.nbirl.bank.model.User;
import com.nbirl.bank.repository.AccountRepository;
import com.nbirl.bank.repository.RoleRepository;
import com.nbirl.bank.repository.UserRepository;
import com.nbirl.bank.repository.VerificationTokenRepository;
import com.nbirl.bank.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.*;

@Controller
@RequestMapping("admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserValidator userValidator;
    @Autowired
    private JavaMailSender javaMailSender;
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%&*-_";
    private static final SecureRandom RANDOM = new SecureRandom();


    public AdminController(UserRepository userRepository,VerificationTokenRepository verificationTokenRepository , RoleRepository roleRepository, AccountRepository accountRepository, BCryptPasswordEncoder passwordEncoder, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
        this.verificationTokenRepository=verificationTokenRepository;
    }

    @GetMapping("/create")
    public String Create(@RequestParam("type") String type, Model model, Authentication authentication) {
        model.addAttribute("user", new User());
        model.addAttribute("type", type.toUpperCase());
        if(!(type.equals("admin")||type.equals("employee")||type.equals("customer")))
                return "error/404";

          if(!authentication.getAuthorities().contains(roleRepository.findByRoleName("CREATE_"+type.toUpperCase())))
            return "error/403";

        if(type.equals("admin"))
            model.addAttribute("permission",authority);
        return "admin/create";
    }
    @GetMapping("/delete")
    public String Delete(@RequestParam("type") String type,Model model, Authentication authentication){
        model.addAttribute("user", new User());
        model.addAttribute("type", type.toUpperCase());
        if(!(type.equals("admin")||type.equals("employee")||type.equals("customer")))
            return "error/404";

        if(!authentication.getAuthorities().contains(roleRepository.findByRoleName("DELETE_"+type.toUpperCase())))
            return "error/403";

        return "/admin/delete";
    }
    @GetMapping("/enable")
    public String Enable(@RequestParam("type") String type,Model model, Authentication authentication){
        model.addAttribute("user", new User());
        model.addAttribute("type", type.toUpperCase());
        if(!(type.equals("admin")||type.equals("employee")||type.equals("customer")))
            return "error/404";

        if(!authentication.getAuthorities().contains(roleRepository.findByRoleName("ENABLE_"+type.toUpperCase())))
            return "error/403";

        return "/admin/enable";
    }

    @GetMapping("/disable")
    public String Disable(@RequestParam("type") String type,Model model, Authentication authentication){
        model.addAttribute("user", new User());
        model.addAttribute("type", type.toUpperCase());
        if(!(type.equals("admin")||type.equals("employee")||type.equals("customer")))
            return "error/404";

        if(!authentication.getAuthorities().contains(roleRepository.findByRoleName("DISABLE_"+type.toUpperCase())))
            return "error/403";

        return "/admin/disable";
    }
    @PostMapping("/create")
    public String PostCreate(@ModelAttribute User user, @RequestParam(required = false) String type,
                             BindingResult bindingResult, Authentication authentication,
                             Model model, RedirectAttributes redirectAttributes){
        user= SetPassword(user);
        userValidator.validate(user, bindingResult);
        System.out.println(user.getPasswordConfirm());
        if (bindingResult.hasErrors()){
            model.addAttribute("type", type);
            if(type.equals("ADMIN"))
            model.addAttribute("permission",authority);
            return "/admin/create";
        }

        if(!authentication.getAuthorities().contains(roleRepository.findByRoleName("CREATE_"+type.toUpperCase())))
            return "error/403";

        userRepository.save(UserChanges(user,type));
        redirectAttributes.addAttribute("success", "true");
        return "/index";
    }

    @PostMapping("/delete")
    public String PostDelete(@ModelAttribute User user, @RequestParam(value = "type", required = false) String type,
                           BindingResult bindingResult, Principal principal,
                             Authentication authentication, Model model){
        User deleteUser= userRepository.findByUsername(user.getUsername());
        if(deleteUser==null)
            bindingResult.rejectValue("username", "user.username.error","Username not found");
        if(!user.getUsername().equalsIgnoreCase(user.getFirstName()))
            bindingResult.rejectValue("firstName", "user.firstName.error","Username doesn't match");
        User adminCheck = userRepository.findByUsername(principal.getName());
        if(!passwordEncoder.matches(user.getPassword(), adminCheck.getPassword()))
            bindingResult.rejectValue("password", "user.password.error","Incorrect Password..!");
        if((!bindingResult.hasErrors()))
            if((type.equals("ADMIN")&&!(deleteUser.getAccountType().equals("ADMIN")))||(type.equals("EMPLOYEE")&&!(deleteUser.getAccountType().equals("EMPLOYEE")))
            ||(type.equals("CUSTOMER")&&((deleteUser.getAccountType().equals("ADMIN")||deleteUser.getAccountType().equals("EMPLOYEE")))))
            bindingResult.rejectValue("username", "user.username.error","Username is not "+type.toLowerCase());
        if(bindingResult.hasErrors()){
            model.addAttribute("type", type);
            return "/admin/delete";}

        if(!authentication.getAuthorities().contains(roleRepository.findByRoleName("DELETE_"+type.toUpperCase())))
            return "error/403";

        verificationTokenRepository.deleteAll(verificationTokenRepository.findByUser(deleteUser));
        userRepository.delete(deleteUser);
        if(type.equals("CUSTOMER"))
            accountRepository.delete(deleteUser.getAccount());
        model.addAttribute("success", "true");
    return "/index";
    }
    @PostMapping("/enable")
    public String PostEnable(@ModelAttribute User user, @RequestParam(value = "type", required = false) String type,
                             BindingResult bindingResult, Principal principal,
                             Authentication authentication, Model model){
        User enableUser= userRepository.findByUsername(user.getUsername());
        if(enableUser==null)
            bindingResult.rejectValue("username", "user.username.error","Username not found");
        if(!user.getUsername().equalsIgnoreCase(user.getFirstName()))
            bindingResult.rejectValue("firstName", "user.firstName.error","Username doesn't match");
        User adminCheck = userRepository.findByUsername(principal.getName());
        if(!passwordEncoder.matches(user.getPassword(), adminCheck.getPassword()))
            bindingResult.rejectValue("password", "user.password.error","Incorrect Password..!");
        if((!bindingResult.hasErrors()))
            if((type.equals("ADMIN")&&!(enableUser.getAccountType().equals("ADMIN")))||(type.equals("EMPLOYEE")&&!(enableUser.getAccountType().equals("EMPLOYEE")))
                ||(type.equals("CUSTOMER")&&((enableUser.getAccountType().equals("ADMIN")||enableUser.getAccountType().equals("EMPLOYEE")))))
            bindingResult.rejectValue("username", "user.username.error","Username is not "+type.toLowerCase());
        if((!bindingResult.hasErrors()))
            if(enableUser.isEnabled())
            bindingResult.rejectValue("username", "user.username.error","User is already enabled");
        if(bindingResult.hasErrors()){
            model.addAttribute("type", type);
            return "/admin/enable";}

        if(!authentication.getAuthorities().contains(roleRepository.findByRoleName("ENABLE_"+type.toUpperCase())))
            return "error/403";

        enableUser.setEnabled(true);
        userRepository.save(enableUser);
        model.addAttribute("success", "true");
        return "/index";
    }
    @PostMapping("/disable")
    public String PostDisable(@ModelAttribute User user, @RequestParam(value = "type", required = false) String type,
                             BindingResult bindingResult, Principal principal,
                              Authentication authentication, Model model){
        User disableUser= userRepository.findByUsername(user.getUsername());
        if(disableUser==null)
            bindingResult.rejectValue("username", "user.username.error","Username not found");
        if(!user.getUsername().equalsIgnoreCase(user.getFirstName()))
            bindingResult.rejectValue("firstName", "user.firstName.error","Username doesn't match");
        User adminCheck = userRepository.findByUsername(principal.getName());
        if(!passwordEncoder.matches(user.getPassword(), adminCheck.getPassword()))
            bindingResult.rejectValue("password", "user.password.error","Incorrect Password..!");
        if((!bindingResult.hasErrors()))
            if((type.equals("ADMIN")&&!(disableUser.getAccountType().equals("ADMIN")))||(type.equals("EMPLOYEE")&&!(disableUser.getAccountType().equals("EMPLOYEE")))
                ||(type.equals("CUSTOMER")&&((disableUser.getAccountType().equals("ADMIN")||disableUser.getAccountType().equals("EMPLOYEE")))))
            bindingResult.rejectValue("username", "user.username.error","Username is not "+type.toLowerCase());
        if((!bindingResult.hasErrors()))
            if(!disableUser.isEnabled())
                bindingResult.rejectValue("username", "user.username.error","User is already disabled");
        if(bindingResult.hasErrors()){
            model.addAttribute("type", type);
            return "/admin/disable";}

        if(!authentication.getAuthorities().contains(roleRepository.findByRoleName("DISABLE_"+type.toUpperCase())))
            return "error/403";

        disableUser.setEnabled(false);
        userRepository.save(disableUser);
        model.addAttribute("success", "true");
        return "/index";
    }


    Map<String,String> authority = new LinkedHashMap<>() {{
        put("CREATE_CUSTOMER","Create Customer");
        put("ENABLE_CUSTOMER", "Enable Customer");
        put("DISABLE_CUSTOMER", "Disable Customer");
        put("DELETE_CUSTOMER", "Delete Customer");
        put("CREATE_EMPLOYEE","Create Employee");
        put("ENABLE_EMPLOYEE", "Enable Employee");
        put("DISABLE_EMPLOYEE", "Disable Employee");
        put("DELETE_EMPLOYEE", "Delete Employee");
        put("CREATE_ADMIN","Create Admin");
        put("ENABLE_ADMIN", "Enable Admin");
        put("DISABLE_ADMIN", "Disable Admin");
        put("DELETE_ADMIN", "Delete Admin");
    }};
    public User UserChanges(User user, String type){
        user.setEnabled(true);
        user.setUsername(user.getUsername().toLowerCase());
        user.setFirstName(user.getFirstName().substring(0, 1).toUpperCase() + user.getFirstName().substring(1).toLowerCase());
        user.setLastName(user.getLastName().substring(0, 1).toUpperCase() + user.getLastName().substring(1).toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmail(user.getEmail().toLowerCase());
        user.setMobileNumber( "+"+ user.getCountrycode() + user.getMobileNumber());
        user.setAccount(accountRepository.findById(0));

        if(type.equals("ADMIN")) {
            user.setAccountType(type);
            String[] roleList = user.getRoleList();
            Set<Role> role = new HashSet<>();
            for(int i=0;i<roleList.length;i++)
                role.add(roleRepository.findByRoleName(roleList[i]));
            role.add(roleRepository.findByRoleName("ROLE_ADMIN"));
            user.setRoles(role);

        }else if(type.equals("EMPLOYEE")){
            user.setAccountType(type);
            Set<Role> role = new HashSet<>();
            role.add(roleRepository.findByRoleName("ROLE_EMPLOYEE"));
            user.setRoles(role);
        }else{
            Account account = new Account();
            account.setAccountNumber("NBI"+randomNumGenerator()+randomNumGenerator());
            user.setAccountType(user.getAccountType());
            account.setBalance(0.0);
            account.setBic("BIC"+randomNumGenerator());
            account.setIban("IE"+randomNumGenerator()+"NBI"+randomNumGenerator()+randomNumGenerator());
            account.setNsc(randomNumGenerator()+"");
            accountRepository.save(account);
            user.setAccount(account);
            Set<Role> role = new HashSet<>();
            role.add(roleRepository.findByRoleName("ROLE_CUSTOMER"));
            user.setRoles(role);
        }
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("NBI Account Created Successfully");
        mailMessage.setFrom("noreply@nbirl.com");
        mailMessage.setText("Hi "+user.getFirstName()+" "+user.getLastName()
                + " your account has been successfully created. Please login your account with the below credentials " +
                "UserName: " +user.getUsername()+
                " Password: "+user.getPasswordConfirm()+
                " please dont share the username or password to anyone. You can change your password in 'My Profile' tab");
        javaMailSender.send(mailMessage);

        return user;
    }
    private int randomNumGenerator() {
        Random random = new Random();
        return random.nextInt(9999);
    }
    private User SetPassword(User user){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; ++i) sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));

         if (!(sb.toString().matches("(?=.*[0-9]).*")))
                sb.append(ALPHABET.charAt(RANDOM.nextInt(10)));
        if (!(sb.toString().matches("(?=.*[a-z]).*")))
                sb.append(ALPHABET.charAt(RANDOM.nextInt(26)+10));
        if (!(sb.toString().matches("(?=.*[A-Z]).*")))
            sb.append(ALPHABET.charAt(RANDOM.nextInt(26)+36));
        if (!(sb.toString().matches("(?=.*[~!@#$%^&*()_-]).*")))
            sb.append(ALPHABET.charAt(RANDOM.nextInt(9)+62));

            user.setPassword(sb.toString());
        user.setPasswordConfirm(sb.toString());
    return user;
    }
}