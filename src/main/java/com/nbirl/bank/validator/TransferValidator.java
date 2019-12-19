package com.nbirl.bank.validator;

import com.nbirl.bank.model.Account;
import com.nbirl.bank.model.User;
import com.nbirl.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


@Component
public class TransferValidator implements Validator {

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> clazz) { return clazz.equals(Account.class); }

    @Override
    public void validate(Object obj, Errors errors) {
        Account account = (Account) obj;
        Account transferAccount =  userService.findByAccountNumber(account.getAccountNumber());
        if (transferAccount == null) {
            errors.rejectValue("accountNumber", "accountNumber.error",
                    "Enter a correct Account Number.");
        }
        else {
            if (!transferAccount.getIban().equals(account.getIban()))
                errors.rejectValue("iban", "iban.error",
                        "IBAN doesn't match with account number.");
            if (!transferAccount.getBic().equals(account.getBic()))
                errors.rejectValue("bic", "bic.error",
                        "BIC doesn't match with account number.");
            if (!transferAccount.getNsc().equals(account.getNsc()))
                errors.rejectValue("nsc", "nsc.error",
                        "NSC doesn't match with account number");
            if (account.getTransfer() <= 0)
                errors.rejectValue("transfer", "transfer.error",
                        "Enter a valid amount to transfer.");
        }

    }
}
