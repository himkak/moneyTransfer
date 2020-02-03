package com.revolut.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.revolut.test.model.Account;
import com.revolut.test.model.AccountState;
import com.revolut.test.model.UserDetails;

public class AccountService {

	private static AccountService instance = null;

	private final AccountRepository accountRepo = AccountRepository.getInstance();

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
		Account account = getNewAccount(userDetails.getUserIdentificationNumber());
		userDetails.setUserAccounts(new HashSet<Account>(Arrays.asList(account)));
		accountRepo.createAccount(userDetails);

	}

	private Account getNewAccount(String userId) {
		return Account.builder().accountNum((int) Math.random()).balance(0).earmarkedAmt(0).state(AccountState.ACTIVE)
				.build();
	}

	public List<UserDetails> getAllUsers() {
		return accountRepo.getAllUsers();
	}

}
