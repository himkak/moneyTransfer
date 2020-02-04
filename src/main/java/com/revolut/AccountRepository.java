package com.revolut;

import java.util.List;
import java.util.Optional;

import javax.persistence.RollbackException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.entity.Account;
import com.revolut.entity.UserDetails;
import com.revolut.util.HibernateUtil;

public class AccountRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);
	private static AccountRepository instance = null;

	private SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

	private AccountRepository() {

	}

	public static AccountRepository getInstance() {
		if (instance == null) {
			synchronized (AccountRepository.class) {
				if (instance == null) {
					instance = new AccountRepository();
				}
			}
		}
		return instance;
	}

	public void saveUserDetails(UserDetails userDetails) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		session.save(userDetails);
		transaction.commit();
		session.close();

	}

	public List<UserDetails> getAllUsers() {
		Session session = sessionFactory.openSession();
		List<UserDetails> usersDetails = session.createQuery("from UserDetails", UserDetails.class).list();
		session.close();
		return usersDetails;
	}

	public Optional<UserDetails> getUser(String userName) {
		Session session = sessionFactory.openSession();
		Query<UserDetails> query = session.createQuery("from UserDetails ud where ud.userName=:userName",
				UserDetails.class);
		query.setParameter("userName", userName);
		UserDetails userDet = query.setMaxResults(1).uniqueResult();
		return Optional.ofNullable(userDet);
	}

	public void saveAccount(Account account) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		session.save(account);
		transaction.commit();
		session.close();

	}

	public boolean addMoney(int amt, int accountId) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		Query<Account> query = session.createQuery("from Account  where accountNum=:accNum", Account.class);
		query.setParameter("accNum", accountId);
		Account fetchedAccDetails = query.setMaxResults(1).uniqueResult();
		if (fetchedAccDetails == null) {
			throw new RuntimeException("account not found.");
		}
		fetchedAccDetails.setBalance(fetchedAccDetails.getBalance() + amt);
		try {
			transaction.commit();
		} catch (RollbackException exc) {
			LOGGER.error("Exception while adding money.", exc);
			return false;
		}
		session.close();
		return true;
	}

	public boolean earMarkAccount(int accountId, int amount) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		Query<Account> query = session.createQuery("from Account  where accountNum=:accNum", Account.class);
		query.setParameter("accNum", accountId);
		Account account = query.setMaxResults(1).uniqueResult();
		account.setBalance(account.getBalance() - amount);
		account.setEarmarkedAmt(account.getEarmarkedAmt() + amount);
		try {
			transaction.commit();
		} catch (RollbackException exc) {
			LOGGER.error("Exception while earmarking amount.", exc);
			return false;
		}
		session.close();
		return true;
	}

	public boolean reduceEarMarkedAmount(int accountId, int amount) {

		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		Query<Account> query = session.createQuery("from Account  where accountNum=:accNum", Account.class);
		query.setParameter("accNum", accountId);
		Account account = query.setMaxResults(1).uniqueResult();
		account.setEarmarkedAmt(account.getEarmarkedAmt() - amount);
		try {
			transaction.commit();
		} catch (RollbackException exc) {
			LOGGER.error("Exception while earmarking amount.", exc);
			return false;
		}
		session.close();
		return true;

	}

	public boolean rollbackEarmarkedAmount(int accountId, int amount) {

		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		Query<Account> query = session.createQuery("from Account  where accountNum=:accNum", Account.class);
		query.setParameter("accNum", accountId);
		Account account = query.setMaxResults(1).uniqueResult();
		account.setBalance(account.getBalance() + amount);
		account.setEarmarkedAmt(account.getEarmarkedAmt() - amount);
		try {
			transaction.commit();
		} catch (RollbackException exc) {
			LOGGER.error("Exception while earmarking amount.", exc);
			return false;
		}
		session.close();
		return true;

	}

	public boolean checkAccountExists(int accountId) {
		Session session = sessionFactory.openSession();
		Query<Account> query = session.createQuery("from Account  where accountNum=:accNum", Account.class);
		query.setParameter("accNum", accountId);
		Account account = query.setMaxResults(1).uniqueResult();
		if(account!=null) {
			return true;
		}
		return false;
	}

	public boolean isSufficientBalanceExists(int accountId, int amount) {
		Session session = sessionFactory.openSession();
		Query<Account> query = session.createQuery("from Account  where accountNum=:accNum", Account.class);
		query.setParameter("accNum", accountId);
		Account account = query.setMaxResults(1).uniqueResult();
		if(account.getBalance()<amount) {
			return false;
		}
		return true;
	}

}
