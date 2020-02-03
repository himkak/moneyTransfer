package com.revolut.test.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * The Class HibernateUtil is used to create the connection with the database
 * and build the session factory.
 * 
 */
public final class HibernateUtil {

	private static SessionFactory sessionFactory;
	
	static {
		getSessionFactory();
	}

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			try {
				sessionFactory = new Configuration().configure().buildSessionFactory();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sessionFactory;
	}

	public static void shutdown() {
		sessionFactory.close();
	}

}
