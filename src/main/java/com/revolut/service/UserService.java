package com.revolut.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.entity.Account;
import com.revolut.entity.UserDetails;
import com.revolut.model.AccountInfo;
import com.revolut.model.CreateAccountRequest;
import com.revolut.model.UserAccountsInfo;
import com.revolut.repository.UserRepository;

public class UserService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	private static UserService instance = null;

	private final UserRepository userRepo = UserRepository.getInstance();


	private UserService() {

	}

	public static UserService getInstance() {
		if (instance == null) {
			synchronized (UserService.class) {
				if (instance == null) {
					instance = new UserService();
				}
			}
		}
		return instance;
	}

	
	public List<UserAccountsInfo> getAllUsers() {
		List<UserDetails> usersDetails = userRepo.getAllUsers();
		List<UserAccountsInfo> userAcc = usersDetails.stream()
				.map(userDet -> UserAccountsInfo.builder().userName(userDet.getUserName())
						.userId(userDet.getUserIdentificationNumber())
						.userAccount(getAccInfo(userDet.getUserAccounts())).build())
				.collect(Collectors.toList());
		return userAcc;
	}
	
	private Set<AccountInfo> getAccInfo(Set<Account> accounts) {
		return accounts.stream()
				.map(acc -> AccountInfo.builder().accountNum(acc.getAccountNum()).balance(acc.getBalance())
						.earmarkedAmt(acc.getEarmarkedAmt()).version(acc.getVersion()).build())
				.collect(Collectors.toSet());
	}

	public UserDetails getUserDetailsIfExistsElseCreate(CreateAccountRequest userDetails) {
		return userRepo.getUser(userDetails.getUserName())
				.orElse(UserDetails.builder().userName(userDetails.getUserName()).build());

	}
}
