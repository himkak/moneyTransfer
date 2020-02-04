package com.revolut.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class TransactionHistoryResponse {
	private int fromAccount;
	private int toAccount;
	private int amount;
	private List<String> states;

}
