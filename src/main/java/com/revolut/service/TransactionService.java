package com.revolut.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.entity.TransactionHistory;
import com.revolut.entity.TransactionState;
import com.revolut.entity.TransactionStatus;
import com.revolut.model.TransactionHistoryResponse;
import com.revolut.repository.TransactionRepository;

public class TransactionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
	private static TransactionService instance = null;

	private final TransactionRepository transactionRepo = TransactionRepository.getInstance();

	private TransactionService() {

	}

	public static TransactionService getInstance() {
		if (instance == null) {
			synchronized (TransactionService.class) {
				if (instance == null) {
					instance = new TransactionService();
				}
			}
		}
		return instance;
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

	public TransactionHistory saveTransaction(int txnId, String frmAcc, String toAcc, int amt) {

		TransactionHistory transHist = new TransactionHistory(frmAcc, toAcc, amt, txnId);
		transactionRepo.saveTransaction(transHist);
		return transHist;
	}
	
	public TransactionState saveTransactionState(TransactionStatus txnStatus, TransactionHistory transHist) {
		TransactionState receiverEarMarkUpdated = new TransactionState(0, txnStatus,
				transHist);
		transactionRepo.saveTransactionState(receiverEarMarkUpdated);
		return receiverEarMarkUpdated;
	}

}
