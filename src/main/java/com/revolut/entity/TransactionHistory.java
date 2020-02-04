package com.revolut.entity;

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

	@OneToMany(cascade=CascadeType.ALL, mappedBy="transaction", fetch=FetchType.LAZY)
	private Set<TransactionState> transStates;

	public TransactionHistory(int fromAccountId, int toAccountId, int amount, int transactionId) {
		super();
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = amount;
		this.transactionId = transactionId;
	}

	@Override
	public String toString() {
		return "TransactionHistory [fromAccountId=" + fromAccountId + ", toAccountId=" + toAccountId + ", amount="
				+ amount + ", transactionId=" + transactionId + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransactionHistory other = (TransactionHistory) obj;
		if (amount != other.amount)
			return false;
		if (fromAccountId != other.fromAccountId)
			return false;
		if (toAccountId != other.toAccountId)
			return false;
		if (transactionId != other.transactionId)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + fromAccountId;
		result = prime * result + toAccountId;
		result = prime * result + transactionId;
		return result;
	}
	
	

}
