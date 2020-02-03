package com.revolut.test;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.revolut.test.model.UserDetails;
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

	public void createAccount(UserDetails userDetails) {
		try {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		session.save(userDetails);
		transaction.commit();
		session.close();
		}catch(Exception exc) {
			exc.printStackTrace();
		}

	}

	public List<UserDetails> getAllUsers() {
		Session session = sessionFactory.openSession();
		return session.createQuery("from UserDetails",UserDetails.class).list();
	}

}
