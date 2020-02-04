package com.revolut.test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.revolut.test.entity.Account;
import com.revolut.test.entity.AccountState;
import com.revolut.test.entity.UserDetails;
import com.revolut.test.model.AccountInfo;
import com.revolut.test.model.AddMoneyRequest;
import com.revolut.test.model.CreateAccountRequest;
import com.revolut.test.model.SendMoneyRequest;
import com.revolut.test.model.UserAccountsInfo;

public class AccountService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
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

	public int createAccount(CreateAccountRequest accntCreationReq) {
		UserDetails userDetails = getUserDetailsIfExistsElseCreate(accntCreationReq);
		Set<Account> accounts = new HashSet<Account>();

		Account account = getNewAccount();
		account.setUserDetails(userDetails);
		accounts.add(account);
		userDetails.setUserAccounts(accounts);
		if (userDetails.getUserIdentificationNumber() == null) {
			accountRepo.saveUserDetails(userDetails);
		} else {
			accountRepo.saveAccount(account);
		}
		return account.getAccountNum();
	}

	private UserDetails getUserDetailsIfExistsElseCreate(CreateAccountRequest userDetails) {
		return accountRepo.getUser(userDetails.getUserName())
				.orElse(UserDetails.builder().userName(userDetails.getUserName()).build());

	}

	private Account getNewAccount() {
		return Account.builder().accountNum(getRandomNumber()).balance(0).earmarkedAmt(0).state(AccountState.ACTIVE)
				.build();
	}

	private int getRandomNumber() {
		return Math.abs((new Random()).nextInt());
	}

	public List<UserAccountsInfo> getAllUsers() {
		List<UserDetails> usersDetails = accountRepo.getAllUsers();
		List<UserAccountsInfo> userAcc = usersDetails.stream()
				.map(userDet -> UserAccountsInfo.builder().userName(userDet.getUserName())
						.userId(userDet.getUserIdentificationNumber())
						.userAccount(getAccInfo(userDet.getUserAccounts())).build())
				.collect(Collectors.toList());
		return userAcc;
	}

	private Set<AccountInfo> getAccInfo(Set<Account> accounts) {
		return accounts.stream().map(acc -> AccountInfo.builder().accountNum(acc.getAccountNum())
				.balance(acc.getBalance()).earmarkedAmt(acc.getEarmarkedAmt()).version(acc.getVersion()).build()).collect(Collectors.toSet());
	}

	public void addMoney(AddMoneyRequest req) {
		accountRepo.addMoney(req.getAmt(), req.getAccountId());

	}

	public int sendMoney(SendMoneyRequest request) {
		int requestId = getRandomNumber();
		MDC.put("requestId", Integer.toString(requestId));
		boolean isEarmarkSuccess = earMarkAccount(request.fromAccountNumber, request.amount);
		if (isEarmarkSuccess) {
			boolean isAmtReceived = addAmtToAccount(request.toAccountNumber, request.amount);
			if (isAmtReceived) {
				reduceTheEarMarkedAmount(request.fromAccountNumber, request.amount);
				LOGGER.info("Transaction successful");
			}else{
				rollbackEarmarkedAmount(request.fromAccountNumber, request.amount);
			}
		}

		return requestId;
	}

	private void rollbackEarmarkedAmount(int fromAccountNumber, int amount) {
		// TODO Auto-generated method stub
		
	}

	private void reduceTheEarMarkedAmount(int fromAccountNumber, int amount) {
		// TODO Auto-generated method stub

	}

	private boolean addAmtToAccount(int toAccountNumber, int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean earMarkAccount(int fromAccountNumber, int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
