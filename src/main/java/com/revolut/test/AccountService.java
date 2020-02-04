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
import com.revolut.test.entity.TransactionHistory;
import com.revolut.test.entity.TransactionState;
import com.revolut.test.entity.TransactionStatus;
import com.revolut.test.entity.UserDetails;
import com.revolut.test.model.AccountInfo;
import com.revolut.test.model.AddMoneyRequest;
import com.revolut.test.model.CreateAccountRequest;
import com.revolut.test.model.SendMoneyRequest;
import com.revolut.test.model.TransactionHistoryResponse;
import com.revolut.test.model.UserAccountsInfo;

public class AccountService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
	private static AccountService instance = null;

	private final AccountRepository accountRepo = AccountRepository.getInstance();

	private final TransactionRepository transactionRepo = TransactionRepository.getInstance();

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
		return accounts.stream()
				.map(acc -> AccountInfo.builder().accountNum(acc.getAccountNum()).balance(acc.getBalance())
						.earmarkedAmt(acc.getEarmarkedAmt()).version(acc.getVersion()).build())
				.collect(Collectors.toSet());
	}

	public void addMoney(AddMoneyRequest req) {
		accountRepo.addMoney(req.getAmt(), req.getAccountId());

	}

	public int sendMoney(SendMoneyRequest request) {
		int requestId = getRandomNumber();
		TransactionHistory transHist = new TransactionHistory(request.getFromAccountNumber(),
				request.getToAccountNumber(), request.getAmount(), requestId);
		transactionRepo.saveTransaction(transHist);
		MDC.put("requestId", Integer.toString(requestId));
		boolean isEarmarkSuccess = accountRepo.earMarkAccount(request.getFromAccountNumber(), request.getAmount());
		transactionRepo.saveTransactionState(new TransactionState(0, TransactionStatus.EARMARKED, transHist));
		if (isEarmarkSuccess) {
			boolean isAmtReceived = accountRepo.addMoney(request.getAmount(), request.getToAccountNumber());
			transactionRepo.saveTransactionState(new TransactionState(0, TransactionStatus.TRANSFERRED, transHist));
			if (isAmtReceived) {
				accountRepo.reduceEarMarkedAmount(request.getFromAccountNumber(), request.getAmount());
				transactionRepo.saveTransactionState(
						new TransactionState(0, TransactionStatus.RECEIVER_EARMARKUPDATED, transHist));
				LOGGER.info("Transaction successful");
				transactionRepo.saveTransactionState(new TransactionState(0, TransactionStatus.SUCCESS, transHist));
			} else {
				accountRepo.rollbackEarmarkedAmount(request.getFromAccountNumber(), request.getAmount());
				transactionRepo.saveTransactionState(new TransactionState(0, TransactionStatus.ROLLEDBACK, transHist));
			}
		}

		return requestId;
	}

	public List<TransactionHistoryResponse> getAllTransactions() {
		List<TransactionHistory> fetchedTxnHisty = transactionRepo.getAllTransactions();

		return fetchedTxnHisty.stream().map(txn -> TransactionHistoryResponse.builder()
				.fromAccount(txn.getFromAccountId()).toAccount(txn.getToAccountId()).states(getStates(txn)).build())
				.collect(Collectors.toList());

	}

	private List<String> getStates(TransactionHistory txn) {
		return txn.getTransStates().stream().map(state->state.getStatus().toString()).collect(Collectors.toList());
	}

}
