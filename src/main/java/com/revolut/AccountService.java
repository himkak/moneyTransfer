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

	public boolean addMoney(AddMoneyRequest req) {
		return accountRepo.addMoney(req.getAmt(), req.getAccountId());

	}

	public int sendMoney(SendMoneyRequest request) {
		int requestId = getRandomNumber();
		
		checkAccounts(request);
		checkBalance(request);
		
		TransactionHistory transHist = new TransactionHistory(request.getFromAccountNumber(),
				request.getToAccountNumber(), request.getAmount(), requestId);
		transactionRepo.saveTransaction(transHist);
		MDC.put("requestId", Integer.toString(requestId));
		boolean isEarmarkSuccess = earMarkReceiverAccount(request, transHist);
		if (isEarmarkSuccess) {
			boolean isAmtReceived = transferToReceiverAccount(request, transHist);
			if (isAmtReceived) {
				updateReceiverEarmark(request, transHist);
				transactionSuccess(transHist);
			} else {
				rollback(request, transHist);
			}
		}

		return requestId;
	}

	private void checkBalance(SendMoneyRequest request) {
		boolean isSufficientAmtExists=accountRepo.isSufficientBalanceExists(request.getFromAccountNumber(), request.getAmount());
		if(!isSufficientAmtExists) {
			throw new RuntimeException("Insufficient balance in sender's account.");
		}
	}

	private void checkAccounts(SendMoneyRequest request) {
		boolean isFromAccExists=accountRepo.checkAccountExists(request.getFromAccountNumber());
		if(!isFromAccExists) {
			throw new RuntimeException("Sender account doesnt exists.");
		}
		boolean isToAccountExists=accountRepo.checkAccountExists(request.getToAccountNumber());
		if(!isToAccountExists) {
			throw new RuntimeException("Receiver account doesnt exists.");
		}
	}

	private void rollback(SendMoneyRequest request, TransactionHistory transHist) {
		TransactionState rolledBack = new TransactionState(0, TransactionStatus.ROLLEDBACK, transHist);
		accountRepo.rollbackEarmarkedAmount(request.getFromAccountNumber(), request.getAmount());
		transactionRepo.saveTransactionState(rolledBack);
		transHist.getTransStates().add(rolledBack);
	}

	private void transactionSuccess(TransactionHistory transHist) {
		TransactionState success = new TransactionState(0, TransactionStatus.SUCCESS, transHist);
		LOGGER.info("Transaction successful");
		transactionRepo.saveTransactionState(success);
		transHist.getTransStates().add(success);
	}

	private void updateReceiverEarmark(SendMoneyRequest request, TransactionHistory transHist) {
		accountRepo.reduceEarMarkedAmount(request.getFromAccountNumber(), request.getAmount());
		TransactionState receiverEarMarkUpdated = new TransactionState(0, TransactionStatus.RECEIVER_EARMARKUPDATED,
				transHist);
		transactionRepo.saveTransactionState(receiverEarMarkUpdated);
		transHist.getTransStates().add(receiverEarMarkUpdated);
	}

	private boolean transferToReceiverAccount(SendMoneyRequest request, TransactionHistory transHist) {
		boolean isAmtReceived = accountRepo.addMoney(request.getAmount(), request.getToAccountNumber());
		TransactionState transferred = new TransactionState(0, TransactionStatus.TRANSFERRED, transHist);
		transactionRepo.saveTransactionState(transferred);
		transHist.getTransStates().add(transferred);
		return isAmtReceived;
	}

	private boolean earMarkReceiverAccount(SendMoneyRequest request, TransactionHistory transHist) {
		boolean isEarmarkSuccess = accountRepo.earMarkAccount(request.getFromAccountNumber(), request.getAmount());
		TransactionState earmarked = new TransactionState(0, TransactionStatus.EARMARKED, transHist);
		transHist.setTransStates(new HashSet<>());
		transHist.getTransStates().add(earmarked);
		transactionRepo.saveTransactionState(earmarked);
		return isEarmarkSuccess;
	}

	public List<TransactionHistoryResponse> getAllTransactions() {
		List<TransactionHistory> fetchedTxnHisty = transactionRepo.getAllTransactions();

		return fetchedTxnHisty.stream().map(txn -> TransactionHistoryResponse.builder()
				.fromAccount(txn.getFromAccountId()).toAccount(txn.getToAccountId()).states(getStates(txn)).build())
				.collect(Collectors.toList());

	}

	private List<String> getStates(TransactionHistory txn) {
		return txn.getTransStates().stream().map(state -> state.getStatus().toString()).collect(Collectors.toList());
	}

}
