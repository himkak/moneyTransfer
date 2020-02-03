package com.revolut.test.model;

import javax.persistence.Entity;

@Entity
public class Account {
	
	private int accountNum;
	private int balance;
	private int earmarkedAmt;
	private String userId;
	private AccountState state; 

}
