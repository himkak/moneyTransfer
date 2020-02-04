package com.revolut.repository;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.revolut.entity.TransactionHistory;
import com.revolut.entity.TransactionState;
import com.revolut.util.HibernateUtil;

public class TransactionRepository {

	//private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRepository.class);
	private static TransactionRepository instance = null;

	private SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

	private TransactionRepository() {

	}

	public static TransactionRepository getInstance() {
		if (instance == null) {

			synchronized (AccountRepository.class) {
				if (instance == null) {
					instance = new TransactionRepository();
				}
			}
		}
		return instance;
	}

	public void saveTransaction(TransactionHistory transactionHistory) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		session.save(transactionHistory);
		transaction.commit();
		session.close();
	}

	public void saveTransactionState(TransactionState transactionState) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		session.save(transactionState);
		transaction.commit();
		session.close();
	}

	public List<TransactionHistory> getAllTransactions() {
		Session session = sessionFactory.openSession();
		List<TransactionHistory> history = session.createQuery("from TransactionHistory", TransactionHistory.class)
				.list();
		history.stream().forEach(h->System.out.println(h.getTransStates()));
		session.close();
		return history;
	}

}
