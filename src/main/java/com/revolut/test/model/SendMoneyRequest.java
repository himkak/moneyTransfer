package com.revolut.test.model;

import lombok.Data;

@Data
public class SendMoneyRequest {

	private int fromAccountNumber;
	private int toAccountNumber;
	private int amount;

}
