package com.revolut.test.model;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAccountsInfo {
	
	private String userName;
	private String userId;
	private Set<AccountInfo> userAccount;
	

}
