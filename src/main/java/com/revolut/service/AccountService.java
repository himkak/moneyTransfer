package com.revolut.service;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.revolut.entity.Account;
import com.revolut.entity.AccountState;
import com.revolut.entity.TransactionHistory;
import com.revolut.entity.TransactionState;
import com.revolut.entity.TransactionStatus;
import com.revolut.entity.UserDetails;
import com.revolut.model.AddMoneyRequest;
import com.revolut.model.CreateAccountRequest;
import com.revolut.model.SendMoneyRequest;
import com.revolut.repository.AccountRepository;

public class AccountService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
	private static AccountService instance = null;

	private final AccountRepository accountRepo = AccountRepository.getInstance();

	private final TransactionService transactionServ = TransactionService.getInstance();

	private final UserService usrServ = UserService.getInstance();

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
		UserDetails userDetails = usrServ.getUserDetailsIfExistsElseCreate(accntCreationReq);
		Set<Account> accounts = new HashSet<Account>();

		Account account = getNewAccount();
		account.setUserDetails(userDetails);
		accounts.add(account);
		userDetails.setUserAccounts(accounts);
		if (userDetails.getUserIdentificationNumber() == null) {
			accountRepo.saveUserAndAccountDetails(userDetails);
		} else {
			accountRepo.saveAccount(account);
		}
		return account.getAccountNum();
	}

	private Account getNewAccount() {
		return Account.builder().accountNum(getRandomNumber()).balance(0).earmarkedAmt(0).state(AccountState.ACTIVE)
				.build();
	}

	private int getRandomNumber() {
		return Math.abs((new Random()).nextInt());
	}

	public boolean addMoney(AddMoneyRequest req) {
		return accountRepo.addMoney(req.getAmt(), req.getAccountId());

	}

	public int sendMoney(SendMoneyRequest request) {
		int requestId = getRandomNumber();
		LOGGER.debug("TransactionId for request :{} , generated is :{}", request, requestId);
		checkAccounts(request);
		checkBalance(request);

		TransactionHistory transHist = transactionServ.saveTransaction(requestId, request.getFromAccountNumber(),
				request.getToAccountNumber(), request.getAmount());
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
		boolean isSufficientAmtExists = accountRepo.isSufficientBalanceExists(request.getFromAccountNumber(),
				request.getAmount());
		if (!isSufficientAmtExists) {
			throw new RuntimeException("Insufficient balance in sender's account.");
		}
	}

	private void checkAccounts(SendMoneyRequest request) {
		boolean isFromAccExists = accountRepo.checkAccountExists(request.getFromAccountNumber());
		if (!isFromAccExists) {
			throw new RuntimeException("Sender account doesnt exists.");
		}
		boolean isToAccountExists = accountRepo.checkAccountExists(request.getToAccountNumber());
		if (!isToAccountExists) {
			throw new RuntimeException("Receiver account doesnt exists.");
		}
	}

	private void rollback(SendMoneyRequest request, TransactionHistory transHist) {
		LOGGER.debug("ROLLEDBACK");
		accountRepo.rollbackEarmarkedAmount(request.getFromAccountNumber(), request.getAmount());
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.ROLLEDBACK, transHist);
		transHist.getTransStates().add(txnState);
	}

	private void transactionSuccess(TransactionHistory transHist) {
		LOGGER.info("Transaction successful");
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.SUCCESS, transHist);
		transHist.getTransStates().add(txnState);
	}

	private void updateReceiverEarmark(SendMoneyRequest request, TransactionHistory transHist) {
		LOGGER.debug("RECEIVER_EARMARKUPDATED");
		accountRepo.reduceEarMarkedAmount(request.getFromAccountNumber(), request.getAmount());
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.RECEIVER_EARMARKUPDATED,
				transHist);
		transHist.getTransStates().add(txnState);
	}

	private boolean transferToReceiverAccount(SendMoneyRequest request, TransactionHistory transHist) {
		LOGGER.debug("TRANSFERRED");
		boolean isAmtReceived = accountRepo.addMoney(request.getAmount(), request.getToAccountNumber());
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.TRANSFERRED, transHist);
		transHist.getTransStates().add(txnState);
		return isAmtReceived;
	}

	private boolean earMarkReceiverAccount(SendMoneyRequest request, TransactionHistory transHist) {
		LOGGER.debug("Earmak done.");
		boolean isEarmarkSuccess = accountRepo.earMarkAccount(request.getFromAccountNumber(), request.getAmount());
		transHist.setTransStates(new HashSet<>());
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.EARMARKED, transHist);
		transHist.getTransStates().add(txnState);
		return isEarmarkSuccess;
	}

}
