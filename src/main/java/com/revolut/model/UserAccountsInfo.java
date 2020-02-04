package com.revolut.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAccountsInfo {
	
	private String userName;
	private String userId;
	private Set<AccountInfo> userAccount;
	

}
