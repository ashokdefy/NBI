package com.nbirl.bank.service;

import com.nbirl.bank.model.Account;
import com.nbirl.bank.model.User;

public interface UserService {
    User findByUsername(String username);
    User findByEmail(String email);
    Account findByAccountNumber(String accountNumber);
    User findByUserId(long id);
    Account findByAccountId(long id);
    void save(User user);
}
