package com.revolut.test.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountInfo {
	
	private int accountNum;
	private int balance;
	private int earmarkedAmt;
	
	private long version;
	

}
