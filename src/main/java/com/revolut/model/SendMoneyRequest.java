package com.revolut.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMoneyRequest {

	private int fromAccountNumber;
	private int toAccountNumber;
	private int amount;

}
