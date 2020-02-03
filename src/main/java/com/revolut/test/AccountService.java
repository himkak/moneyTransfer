package com.revolut.test;

import com.revolut.test.model.UserDetails;

public class AccountService {

	private static AccountService instance = null;

	private AccountRepository accountRepo=AccountRepository.getInstance();
	
	private AccountService() {
		
	}

	public static AccountService getInstance() {
		if (instance == null) {
			synchronized (AccountService.class) {
				if (instance == null) {
					instance = new AccountService();
				}
			}
		}
		return instance;
	}

	public void createAccount(UserDetails userDetails) {
		accountRepo.createAccount(userDetails);
		
		
	}

}
