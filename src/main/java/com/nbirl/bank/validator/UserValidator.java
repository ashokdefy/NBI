package com.nbirl.bank.validator;

import com.nbirl.bank.model.User;
import com.nbirl.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


@Component
public class UserValidator implements Validator {

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> clazz) { return clazz.equals(User.class); }

    @Override
    public void validate(Object obj, Errors errors) {
        User user = (User) obj;
        if (user.getUsername().length() < 6) {
            errors.rejectValue("username", "user.username.error","Username must have at least 6 characters.");
        }
        else if(user.getUsername().length() > 32){
            errors.rejectValue("username","user.username.error","Username can't have more than 32 characters.");
        }
        if(user.getUsername().matches("^.*[^a-zA-Z0-9 ].*$")){
            errors.rejectValue("username","user.username.error","Username should be alphanumeric.");
        }

        //Username can't be duplicated
        if (userService.findByUsername(user.getUsername()) != null) {
            errors.rejectValue("username", "user.username.error", "This username is already in use.");
        }
        //Email can't be duplicated
        if (userService.findByEmail(user.getEmail()) != null){
            errors.rejectValue("email", "user.email.error","This E-mail is already in use.");
        }
        //Password must have at least 8 characters and max 32
        if (user.getPassword().length() < 8) {
            errors.rejectValue("password", "user.password.error","Password must have at least 8 characters.");
        }
        else if (user.getPassword().length() > 32){
            errors.rejectValue("password","user.password.error", "Password can't have more than 32 characters.");
        }
        else if (!(user.getPassword().matches("(?=.*[0-9]).*")
                && user.getPassword().matches("(?=.*[a-z]).*")
                    && user.getPassword().matches("(?=.*[A-Z]).*")
                        && user.getPassword().matches("(?=.*[~!@#$%^&*()_-]).*"))){
            errors.rejectValue("password","user.password.error", "Password must have atleast one Uppercase, Lowercase, Numeric and a Symbol.");
        }

        if (!(user.getMobileNumber() != null &&
                user.getMobileNumber().matches("[0-9]+") &&
                (user.getMobileNumber().length() > 8) && (user.getMobileNumber().length() < 14))) {
            errors.rejectValue("mobileNumber", "user.mobile.error", "Enter a correct mobile number");
        }
        if (!user.getPasswordConfirm().equals(user.getPassword())) {
            errors.rejectValue("passwordConfirm","user.password.error", "Passwords needs to be the same.");
        }
    }
}
