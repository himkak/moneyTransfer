package com.revolut.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.entity.UserDetails;
import com.revolut.util.HibernateUtil;

public class UserRepository {
	
	private SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	private static final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);
	private static UserRepository instance = null;


	private UserRepository() {

	}

	public static UserRepository getInstance() {
		if (instance == null) {
			synchronized (UserRepository.class) {
				if (instance == null) {
					instance = new UserRepository();
				}
			}
		}
		return instance;
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

}
