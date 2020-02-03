package com.revolut.test;

import java.util.List;

import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;

import com.revolut.test.model.UserDetails;
import com.revolut.test.util.HibernateUtil;


public class AccountRepositoryTest {

	@Test
	public void shouldSaveUserDetailsInDB_when_userDetailsProvided() {
		UserDetails userDetails = UserDetails.builder().userName("him").userIdentificationNumber("123").build();
		AccountRepository.getInstance().createAccount(userDetails);

		Session session = HibernateUtil.getSessionFactory().openSession();
		List<UserDetails> usersDetails = session.createQuery("from UserDetails", UserDetails.class)
				.getResultList();
		
		Assert.assertEquals(1, usersDetails.size());
	}

}