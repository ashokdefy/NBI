package com.nbirl.bank.service;

import com.nbirl.bank.model.Account;
import com.nbirl.bank.model.Role;
import com.nbirl.bank.model.User;
import com.nbirl.bank.repository.AccountRepository;
import com.nbirl.bank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findByUserId(long id) {
        return userRepository.findById(id);
    }

    @Override
    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public Account findByAccountId(long id) {
        return  accountRepository.findById(id);
    }

    @Override
    public void save(User user) {
        user.setUsername(user.getUsername().toLowerCase());
        user.setFirstName(user.getFirstName().substring(0, 1).toUpperCase() + user.getFirstName().substring(1).toLowerCase());
        user.setLastName(user.getLastName().substring(0, 1).toUpperCase() + user.getLastName().substring(1).toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmail(user.getEmail().toLowerCase());
        user.setEnabled(false);

        Role role = roleService.findByRoleName("ROLE_CUSTOMER");
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user.setMobileNumber( "+"+ user.getCountrycode() + user.getMobileNumber());
        Account account = new Account();
        account.setAccountNumber("NBI"+randomNumGenerator()+randomNumGenerator());
        user.setAccountType(user.getAccountType());
        account.setBalance(0.0);
        account.setBic("BIC"+randomNumGenerator());
        account.setIban("IE"+randomNumGenerator()+"NBI"+randomNumGenerator()+randomNumGenerator());
        account.setNsc(randomNumGenerator()+"");
        accountRepository.save(account);
        user.setAccount(account);
        userRepository.save(user);
    }
    private int randomNumGenerator() {
        Random random = new Random();
        return random.nextInt(9999);
    }

}
