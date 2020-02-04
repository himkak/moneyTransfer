package com.revolut.test;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.revolut.test.entity.Account;
import com.revolut.test.entity.UserDetails;
import com.revolut.test.util.HibernateUtil;

public class AccountRepository {

	private static AccountRepository instance = new AccountRepository();

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

	public void addMoney(int amt, int accountId) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		Query<Account> query = session.createQuery("from Account  where accountNum=:accNum", Account.class);
		query.setParameter("accNum", accountId);
		Account fetchedAccDetails = query.getSingleResult();
		fetchedAccDetails.setBalance(fetchedAccDetails.getBalance() + amt);
		transaction.commit();
		session.close();
	}

}
