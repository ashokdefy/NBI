package com.nbirl.bank.service.auth;

import com.nbirl.bank.athorizer.LoginAttemptService;
import com.nbirl.bank.model.User;
import com.nbirl.bank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LoginAttemptService loginAttemptService;
    @Autowired
    private HttpServletRequest request;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        String ip = request.getRemoteAddr();
        if (loginAttemptService.isBlocked(ip)) {
            logger.error("User "+username+" has been blocked for brute forcing");
            throw new UsernameNotFoundException(String.format("User with username=%s has been blocked ", username));
        }
        User user = userRepository.findByUsername(username);
        if (user != null) {
            Set<GrantedAuthority> authorities = new HashSet<>();
            Iterator iterator = user.getRoles().iterator();
            while (iterator.hasNext())
                authorities.add((GrantedAuthority) iterator.next());
            return new org.springframework.security.core.userdetails.User(user.getUsername(),
                    user.getPassword(),user.isEnabled(), true, true,true, authorities);
        }
        else
            throw new UsernameNotFoundException(String.format("User with username=%s was not found", username));
    }

}

