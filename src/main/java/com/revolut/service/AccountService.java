package com.revolut.service;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.revolut.application.ConfigurationLoader;
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

	private int totalRetryCount = Integer.parseInt(ConfigurationLoader.getInstance().getProperties("totalRetryCount","5"));
	private int retryInterval = Integer.parseInt(ConfigurationLoader.getInstance().getProperties("retryInterval","1000"));

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

	public String createAccount(CreateAccountRequest accntCreationReq) {
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
		return Account.builder().balance(0).earmarkedAmt(0).state(AccountState.ACTIVE).build();
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
		boolean isEarmarkSuccess = earMarkReceiverAccount(request, transHist, requestId);
		if (isEarmarkSuccess) {
			boolean isAmtReceived = transferToReceiverAccount(request, transHist, requestId);
			if (isAmtReceived) {
				updateReceiverEarmark(request, transHist, requestId);
				transactionSuccess(transHist, requestId);
			} else {
				rollback(request, transHist, requestId);
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

	private void rollback(SendMoneyRequest request, TransactionHistory transHist, int reqId) {
		LOGGER.debug("Going to ROLLEDBACK., reqId:{}", reqId);
		Supplier<Boolean> fnc = () -> accountRepo.rollbackEarmarkedAmount(request.getFromAccountNumber(),
				request.getAmount());
		retry(fnc);
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.ROLLEDBACK, transHist);
		transHist.getTransStates().add(txnState);
		LOGGER.debug("ROLLEDBACK, reqId:{}", reqId);
	}

	private void transactionSuccess(TransactionHistory transHist, int reqId) {
		LOGGER.debug("Going to Transaction successful., reqId:{}", reqId);
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.SUCCESS, transHist);
		transHist.getTransStates().add(txnState);
		LOGGER.info("Transaction successful, reqId:{}", reqId);
	}

	private void updateReceiverEarmark(SendMoneyRequest request, TransactionHistory transHist, int reqId) {
		LOGGER.debug("Going to RECEIVER_EARMARKUPDATED., reqId:{}", reqId);
		Supplier<Boolean> fnc = () -> accountRepo.reduceEarMarkedAmount(request.getFromAccountNumber(),
				request.getAmount());
		retry(fnc);
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.RECEIVER_EARMARKUPDATED,
				transHist);
		transHist.getTransStates().add(txnState);
		LOGGER.debug("RECEIVER_EARMARKUPDATED , reqId:{}", reqId);
	}

	private boolean transferToReceiverAccount(SendMoneyRequest request, TransactionHistory transHist, int reqId) {
		LOGGER.debug("Going to TRANSFERRED., reqId:{}", reqId);
		Supplier<Boolean> fnc = () -> accountRepo.addMoney(request.getAmount(), request.getToAccountNumber());
		boolean isAmtReceived = retry(fnc);
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.TRANSFERRED, transHist);
		transHist.getTransStates().add(txnState);
		LOGGER.debug("TRANSFERRED, reqId:{}", reqId);
		return isAmtReceived;
	}

	private boolean earMarkReceiverAccount(SendMoneyRequest request, TransactionHistory transHist, int reqId) {
		LOGGER.debug("Going to Earmak., reqId:{}", reqId);
		Supplier<Boolean> fnc = () -> accountRepo.earMarkAccount(request.getFromAccountNumber(), request.getAmount());
		boolean isEarmarkSuccess = retry(fnc);
		transHist.setTransStates(new HashSet<>());
		TransactionState txnState = transactionServ.saveTransactionState(TransactionStatus.EARMARKED, transHist);
		transHist.getTransStates().add(txnState);
		LOGGER.debug("Earmak done., reqId:{}", reqId);
		return isEarmarkSuccess;
	}

	private boolean retry(Supplier<Boolean> fnc) {

		boolean isOpSuccess = false;
		int retryCount = 0;
		while (!isOpSuccess && retryCount < totalRetryCount) {
			LOGGER.debug("Going to retry.");
			isOpSuccess = fnc.get();
			try {
				
				Thread.sleep(retryInterval);
			} catch (InterruptedException e) {
				LOGGER.warn("Exception while retrying");
			}
			retryCount++;
			LOGGER.debug("Retry result:{}, count:{}",isOpSuccess, retryCount);
		}
		return isOpSuccess;

	}

}
