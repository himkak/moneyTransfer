package com.revolut.test;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.Assert;
import org.junit.Test;

import com.revolut.entity.UserDetails;
import com.revolut.repository.AccountRepository;
import com.revolut.util.HibernateUtil;

public class AccountRepositoryTest {

	@Test
	public void shouldSaveUserDetailsInDB_when_userDetailsProvided() {
		UserDetails userDetails = UserDetails.builder().userName("hima").userIdentificationNumber("123").build();
		AccountRepository.getInstance().saveUserAndAccountDetails(userDetails);

		Session session = HibernateUtil.getSessionFactory().openSession();
		Query<UserDetails> query = session.createQuery("from UserDetails where userName=:userName", UserDetails.class);
		query.setParameter("userName", "hima");
		List<UserDetails> usersDetails = query.getResultList();
		Assert.assertEquals(1, usersDetails.size());
	}

}
