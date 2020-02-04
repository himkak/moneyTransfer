package com.revolut.test.model;

import lombok.Data;

@Data
public class AddMoneyRequest {

	private int accountId;
	private int amt;

}
