package com.revolut.test.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Account")
public class Account {
	
	@Id
	private int accountNum;
	private int balance;
	private int earmarkedAmt;
	//private String userId;
	private AccountState state; 

}
