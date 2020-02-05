package com.revolut.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class TransactionHistoryResponse {
	private String fromAccount;
	private String toAccount;
	private int amount;
	private List<String> states;

}
