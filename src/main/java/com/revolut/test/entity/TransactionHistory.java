package com.revolut.test.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistory {

	private int fromAccountId;
	private int toAccountId;
	private int amount;
	@Id
	private int transactionId;

	//@Fetch(FetchMode.SUBSELECT)
	@OneToMany(cascade=CascadeType.ALL, mappedBy="transaction", fetch=FetchType.LAZY)
	//@Transient
	private Set<TransactionState> transStates;

	public TransactionHistory(int fromAccountId, int toAccountId, int amount, int transactionId) {
		super();
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = amount;
		this.transactionId = transactionId;
	}

}
