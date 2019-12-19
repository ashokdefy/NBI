package com.nbirl.bank.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name= "account_number")
    private String accountNumber;

    @Transient
    private double transfer;

    @Column
    private double balance;

    @NotNull
    @Column
    private String iban;

    @NotNull
    @Column
    private String bic;

    @NotNull
    @Column
    private String nsc;

    public Account(){}

    public Account(String accountNumber) {
        this.accountNumber= accountNumber;
        this.iban=" ";
        this.nsc = " ";
        this.bic=" ";
        this.balance=0;
    }
}
