package com.revolut.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMoneyRequest {

	private String fromAccountNumber;
	private String toAccountNumber;
	private int amount;

}
