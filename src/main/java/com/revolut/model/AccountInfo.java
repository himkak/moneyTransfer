package com.revolut.test.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfo {
	
	private int accountNum;
	private int balance;
	private int earmarkedAmt;
	
	private long version;
	

}
