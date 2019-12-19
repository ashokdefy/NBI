package com.nbirl.bank.repository;

import com.nbirl.bank.model.User;
import com.nbirl.bank.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
   VerificationToken findByToken(String token);
   List<VerificationToken> findByUser(User user);
}

