package com.revolut.test.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Account")
public class Account {

	@Id
	private int accountNum;
	private int balance;
	private int earmarkedAmt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private UserDetails userDetails;
	private AccountState state;
	
	@Version
	private long version; 

	@Override
	public String toString() {
		return "Account [accountNum=" + accountNum + ", balance=" + balance + ", earmarkedAmt=" + earmarkedAmt
				+ ", state=" + state + "]";
	}

}
